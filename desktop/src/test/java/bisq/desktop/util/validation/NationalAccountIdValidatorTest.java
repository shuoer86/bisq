package bisq.desktop.util.validation;

import bisq.core.locale.Res;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NationalAccountIdValidatorTest {
    @BeforeEach
    public void setup() {
        Locale.setDefault(new Locale("en", "US"));
        Res.setBaseCurrencyCode("BTC");
        Res.setBaseCurrencyName("Bitcoin");
    }

    @Test
    public void testValidationForArgentina(){
        NationalAccountIdValidator validator = new NationalAccountIdValidator("AR");
        assertTrue(validator.validate("2850590940090418135201").isValid);
        final String wrongNationalAccountId = "285059094009041813520";
        assertFalse(validator.validate(wrongNationalAccountId).isValid);
        assertEquals("CBU number must consist of 22 numbers.", validator.validate(wrongNationalAccountId).errorMessage);
    }
}
