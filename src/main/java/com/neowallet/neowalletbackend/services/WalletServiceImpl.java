package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.P2pDto;
import com.neowallet.neowalletbackend.dto.PaymentGatewayDto;
import com.neowallet.neowalletbackend.exception.PaymentFailedException;
import com.neowallet.neowalletbackend.exception.WalletBlockedException;
import com.neowallet.neowalletbackend.model.*;
import com.neowallet.neowalletbackend.repository.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PaymentService paymentService;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final FraudDetectionService fraudDetectionService;

    public WalletServiceImpl(WalletRepository walletRepository, IdempotencyRepository idempotencyRepository, UserRepository userRepository, TransactionRepository transactionRepository, LedgerEntryRepository ledgerEntryRepository, PaymentService paymentService, PaymentAttemptRepository paymentAttemptRepository, FraudDetectionService fraudDetectionService) {
        this.walletRepository = walletRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.paymentService = paymentService;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.fraudDetectionService = fraudDetectionService;
    }

    @Override
    @Transactional
    public PaymentGatewayDto addMoneyWallet(Long userId, BigDecimal amount, String idempotencyKey, PaymentGateway gateway) {
        User user=userRepository.findById(userId).orElseThrow(()->
                new UsernameNotFoundException("userId does not exist"));
        Wallet wallet=walletRepository.findByUser_UserId(userId).
                orElseThrow(()->new RuntimeException("Something went wrong!!"));


        if(wallet.getStatus()== WalletStatus.BLOCKED){
            throw new WalletBlockedException("Account is under Review !! please wait for some time");
        }
        if(idempotencyRepository.existsByIdempotencyKey(idempotencyKey)){
            throw new RuntimeException("Duplicate request");
        }
        IdempotencyKey key=new IdempotencyKey();
        key.setIdempotencyKey(idempotencyKey);
        key.setRequestHash("ADD_MONEY_"+userId+"_"+amount);
        key.setCreatedAt(LocalDateTime.now());
        key.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        idempotencyRepository.save(key);



        Transaction transaction=new Transaction();
        transaction.setWallet(wallet);
        transaction.setKey(key);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.ADD_MONEY);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        fraudDetectionService.checkAndFlag(transaction);


        PaymentGatewayDto paymentGateway = paymentService.paymentAttempt(transaction,gateway);
        if (paymentGateway.getStatus().equals("failed")){
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new PaymentFailedException("Payment failed plz try again in sometime");
        }


        if (gateway == PaymentGateway.MOCK_BANK) {
            confirmTransaction(paymentGateway.getPaymentId());
        }
        return paymentGateway;
    }

    @Transactional
    @Override
    public P2pDto transfer(Long fromUserId, Long toUserId, BigDecimal amount, String idempotencyKey) {
        User user1=userRepository.findById(toUserId).orElseThrow(()->
                new RuntimeException("User not found"));
        User user2 =userRepository.findById(fromUserId).orElseThrow(()->
                new RuntimeException("User cannot be found"));

        Wallet wallet1=walletRepository.findByUser_UserId(user1.getUserId()).orElseThrow(()->
                new RuntimeException("Something went wrong"));

        Wallet wallet2=walletRepository.findByUser_UserId(user2.getUserId()).orElseThrow(()->
                new RuntimeException("Something went wrong")
        );
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("Cannot transfer to same wallet");
        }


        if (wallet1.getStatus()==WalletStatus.BLOCKED){
            throw new WalletBlockedException("The user you are sending is current blocked " +
                    "for transactions!!, please wait for sometime");
        }
        if (wallet2.getStatus()==WalletStatus.BLOCKED){
            throw new WalletBlockedException("Your account is still under review!");
        }
        if (idempotencyRepository.existsByIdempotencyKey(idempotencyKey)){
            throw new RuntimeException("Duplicate Request !!");
        }
        IdempotencyKey key = new IdempotencyKey();
        key.setIdempotencyKey(idempotencyKey);
        key.setRequestHash("P2P_TRANSFER_"+user1.getUserId()+"_FROM_"+user2.getUserId());
        key.setCreatedAt(LocalDateTime.now());
        key.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        idempotencyRepository.save(key);

        if (wallet2.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient Balance !!");
        }

        Transaction transaction=new Transaction();
        transaction.setAmount(amount);
        transaction.setKey(key);
        transaction.setWallet(wallet2);
        transaction.setTransactionType(TransactionType.P2P);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        fraudDetectionService.checkAndFlag(transaction);

        LedgerEntry senderDebit=new LedgerEntry();//for sending
        senderDebit.setAmount(amount);
        senderDebit.setTransaction(transaction);
        senderDebit.setWallet(wallet2);
        senderDebit.setLedgerType(LedgerType.DEBIT);
        senderDebit.setBalanceAfter(wallet2.getBalance().subtract(amount));
        senderDebit.setCreatedAt(LocalDateTime.now());
        ledgerEntryRepository.save(senderDebit);


        LedgerEntry receiverCredit=new LedgerEntry();
        receiverCredit.setAmount(amount);
        receiverCredit.setTransaction(transaction);
        receiverCredit.setWallet(wallet1);
        receiverCredit.setLedgerType(LedgerType.CREDIT);
        receiverCredit.setBalanceAfter(wallet1.getBalance().add(amount));
        receiverCredit.setCreatedAt(LocalDateTime.now());
        ledgerEntryRepository.save(receiverCredit);

        wallet1.setBalance(wallet1.getBalance().add(amount));
        wallet2.setBalance(wallet2.getBalance().subtract(amount));
        walletRepository.save(wallet2);
        walletRepository.save(wallet1);

        P2pDto response=new P2pDto();
        response.setFromUserId(user2.getUserId());
        response.setToUserId(user1.getUserId());
        response.setAmount(amount);
        response.setMessage("Sent Money from "+user2.getUserId()+"to "+user1.getUserId());

        return response;
    }

    @Transactional
    @Override
    public Wallet confirmTransaction(Long paymentAttemptId) {
        PaymentAttempt attempt=paymentAttemptRepository.
                findById(paymentAttemptId).orElseThrow(
                        ()-> new RuntimeException("Payment attempt not found"));

        if (attempt.getStatus() != PaymentStatus.CAPTURED){
            throw new RuntimeException("Payment not successful,cannot confirm transaction");
        }
        Transaction transaction=attempt.getTransaction();

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING){
            throw new RuntimeException("Transaction already processed");
        }
        Wallet wallet=transaction.getWallet();
        BigDecimal newBalance=wallet.getBalance().add(transaction.getAmount());

        LedgerEntry ledgerEntry=new LedgerEntry();
        ledgerEntry.setWallet(wallet);
        ledgerEntry.setTransaction(transaction);
        ledgerEntry.setAmount(transaction.getAmount());
        ledgerEntry.setLedgerType(LedgerType.CREDIT);
        ledgerEntry.setBalanceAfter(newBalance);
        ledgerEntry.setCreatedAt(LocalDateTime.now());

        ledgerEntryRepository.save(ledgerEntry);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
        return wallet;
    }
}
