package it.pagopa.pn.apikey.manager.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskDataUtils {

    public static String maskInformation(String dataBuffered){
        Pattern virtualKey = Pattern.compile("(\"virtualKey\")\\s*:\\s*\"(.*?)\"");

        dataBuffered = maskMatcher(virtualKey, dataBuffered);

        return dataBuffered;
    }

    private static String maskMatcher(Pattern pattern, String dataBuffered){
        Matcher matcher = pattern.matcher(dataBuffered);
        while(matcher.find()){
            String toBeMasked = matcher.group(2);
            String valueMasked = mask(toBeMasked);
            if(!toBeMasked.isBlank()){
                dataBuffered = dataBuffered.replace("\""+toBeMasked+"\"","\""+valueMasked+"\"");
            }
        }
        return dataBuffered;
    }

    private static String mask(String unmasked){
        if(unmasked.contains("-"))
            return maskVirtualKey(unmasked);
        else
            return maskString(unmasked);
    }


    private static String maskVirtualKey(String strAddress){
        String[] parts = strAddress.split("-");
        String masked = "";
        for (String part : parts)
            masked = masked + maskString(part) + ",";
        return masked.substring(0,masked.length()-1);
    }

    private static String maskString(String strText) {
        int start = 1;
        int end = strText.length()-3;
        String maskChar = String.valueOf('*');

        if(strText.equals(""))
            return "";
        if(strText.length() < 4){
            end = strText.length();
        }
        int maskLength = end - start;
        if(maskLength == 0)
            return maskChar;
        String sbMaskString = maskChar.repeat(Math.max(0, maskLength));
        return strText.substring(0, start)
                + sbMaskString
                + strText.substring(start + maskLength);
    }

}

