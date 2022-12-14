package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.NONE)
public class MaskDataUtils {

    public static String maskInformation(String data) {
        Pattern virtualKey1 = Pattern.compile("(\"value\")\\s*:\\s*\"(.*?)\"");
        Pattern virtualKey2 = Pattern.compile("(\"apiKey\")\\s*:\\s*\"(.*?)\"");

        String dynamoDbPrefix = "(" + ApiKeyConstant.VIRTUAL_KEY + "|" + AggregationConstant.AWS_API_KEY + ")";
        Pattern dynamoDb = Pattern.compile(dynamoDbPrefix + "=AttributeValue\\(S=(.*?)\\)");

        data = maskMatcher(virtualKey1, data);
        data = maskMatcher(virtualKey2, data);
        data = maskMatcher(dynamoDb, data);

        return data;
    }

    public static String maskValue(String data) {
        Pattern pattern = Pattern.compile("([v|V]alue)=(.*?),");
        return maskMatcher(pattern, data);
    }

    private static String maskMatcher(Pattern pattern, String dataBuffered) {
        Matcher matcher = pattern.matcher(dataBuffered);
        while (matcher.find()) {
            String toBeMasked = matcher.group(2);
            String valueMasked = mask(toBeMasked);
            if (!toBeMasked.isBlank()) {
                dataBuffered = dataBuffered.replace(toBeMasked, valueMasked);
            }
        }
        return dataBuffered;
    }

    private static String mask(String unmasked) {
        if (unmasked.contains("-")) {
            return maskVirtualKey(unmasked);
        } else {
            return maskString(unmasked);
        }
    }

    private static String maskVirtualKey(String strAddress) {
        String[] parts = strAddress.split("-");
        StringBuilder masked = new StringBuilder();
        for (String part : parts) {
            masked.append(maskString(part)).append("-");
        }
        return masked.substring(0, masked.length() - 1);
    }

    private static String maskString(String strText) {
        int start = 1;
        int end = strText.length() - 1;
        String maskChar = String.valueOf('*');

        if (strText.equals("")) {
            return "";
        }
        if (strText.length() <= 3) {
            return maskChar.repeat(strText.length());
        }
        int maskLength = end - start;
        String sbMaskString = maskChar.repeat(Math.max(0, maskLength));
        return strText.substring(0, start)
                + sbMaskString
                + strText.substring(start + maskLength);
    }
}
