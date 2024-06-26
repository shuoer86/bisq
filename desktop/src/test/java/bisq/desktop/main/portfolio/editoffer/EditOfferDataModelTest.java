package bisq.desktop.main.portfolio.editoffer;

import bisq.desktop.util.validation.SecurityDepositValidator;

import bisq.core.account.witness.AccountAgeWitnessService;
import bisq.core.btc.model.AddressEntry;
import bisq.core.btc.wallet.BsqWalletService;
import bisq.core.btc.wallet.BtcWalletService;
import bisq.core.locale.Country;
import bisq.core.locale.CryptoCurrency;
import bisq.core.locale.GlobalSettings;
import bisq.core.locale.Res;
import bisq.core.offer.OfferDirection;
import bisq.core.offer.OfferUtil;
import bisq.core.offer.OpenOffer;
import bisq.core.offer.bisq_v1.CreateOfferService;
import bisq.core.payment.CryptoCurrencyAccount;
import bisq.core.payment.PaymentAccount;
import bisq.core.provider.fee.FeeService;
import bisq.core.provider.price.MarketPrice;
import bisq.core.provider.price.PriceFeedService;
import bisq.core.trade.statistics.TradeStatisticsManager;
import bisq.core.user.Preferences;
import bisq.core.user.User;
import bisq.core.util.coin.BsqFormatter;
import bisq.core.util.validation.InputValidator;

import org.bitcoinj.core.Coin;

import javafx.beans.property.SimpleIntegerProperty;

import javafx.collections.FXCollections;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static bisq.desktop.maker.OfferMaker.btcBCHCOffer;
import static bisq.desktop.maker.PreferenceMakers.empty;
import static com.natpryce.makeiteasy.MakeItEasy.make;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EditOfferDataModelTest {

    private EditOfferDataModel model;
    private User user;

    @BeforeEach
    public void setUp() {

        final CryptoCurrency btc = new CryptoCurrency("BTC", "bitcoin");
        GlobalSettings.setDefaultTradeCurrency(btc);
        Res.setup();

        FeeService feeService = mock(FeeService.class);
        AddressEntry addressEntry = mock(AddressEntry.class);
        BtcWalletService btcWalletService = mock(BtcWalletService.class);
        PriceFeedService priceFeedService = mock(PriceFeedService.class);
        user = mock(User.class);
        PaymentAccount paymentAccount = mock(PaymentAccount.class);
        Preferences preferences = mock(Preferences.class);
        BsqFormatter bsqFormatter = mock(BsqFormatter.class);
        BsqWalletService bsqWalletService = mock(BsqWalletService.class);
        SecurityDepositValidator securityDepositValidator = mock(SecurityDepositValidator.class);
        AccountAgeWitnessService accountAgeWitnessService = mock(AccountAgeWitnessService.class);
        CreateOfferService createOfferService = mock(CreateOfferService.class);
        OfferUtil offerUtil = mock(OfferUtil.class);

        when(btcWalletService.getOrCreateAddressEntry(anyString(), any())).thenReturn(addressEntry);
        when(btcWalletService.getBalanceForAddress(any())).thenReturn(Coin.valueOf(1000L));
        when(priceFeedService.updateCounterProperty()).thenReturn(new SimpleIntegerProperty());
        when(priceFeedService.getMarketPrice(anyString())).thenReturn(
                new MarketPrice("USD",
                        12684.0450,
                        Instant.now().getEpochSecond(),
                        true));
        when(feeService.getTxFee(anyInt())).thenReturn(Coin.valueOf(1000L));
        when(user.findFirstPaymentAccountWithCurrency(any())).thenReturn(paymentAccount);
        when(user.getPaymentAccountsAsObservable()).thenReturn(FXCollections.observableSet());
        when(securityDepositValidator.validate(any())).thenReturn(new InputValidator.ValidationResult(false));
        when(accountAgeWitnessService.getMyTradeLimit(any(), any(), any())).thenReturn(100000000L);
        when(preferences.getUserCountry()).thenReturn(new Country("US", "United States", null));
        when(bsqFormatter.formatCoin(any())).thenReturn("0");
        when(bsqWalletService.getAvailableBalance()).thenReturn(Coin.ZERO);

        model = new EditOfferDataModel(createOfferService,
                null,
                offerUtil,
                btcWalletService,
                bsqWalletService,
                empty,
                user,
                null,
                priceFeedService,
                accountAgeWitnessService,
                feeService,
                null,
                null,
                mock(TradeStatisticsManager.class),
                null);
    }

    @Test
    public void testEditOfferOfRemovedAsset() {

        final CryptoCurrencyAccount bitcoinClashicAccount = new CryptoCurrencyAccount();
        bitcoinClashicAccount.setId("BCHC");

        when(user.getPaymentAccount(anyString())).thenReturn(bitcoinClashicAccount);

        model.applyOpenOffer(new OpenOffer(make(btcBCHCOffer)));
        assertNull(model.getPreselectedPaymentAccount());
    }

    @Test
    public void testInitializeEditOfferWithRemovedAsset() {
        assertThrows(IllegalArgumentException.class, () -> model.initWithData(OfferDirection.BUY, null));
    }
}
