package com.github.walterfan.msa.common.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author walter
 *
 */
public class EncodeUtils {

    private EncodeUtils() {

    }

	public static final byte[] int2ByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };
	}

	public static int byteArray2Int(byte[] b) {
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}

    
    public static String decode(ByteBuffer buffer)
    {
        Charset charset  =   null ;
        CharsetDecoder decoder  =   null ;
        CharBuffer charBuffer  =   null ;
        try
        {
            charset  =  Charset.forName( "utf-8" );
            decoder  =  charset.newDecoder();
            charBuffer  =  decoder.decode(buffer);
            return  charBuffer.toString();
        }
        catch  (Exception ex)
        {
            ex.printStackTrace();
            return   "" ;
        }
    } 

    
    public static String byte2Hex(byte[] b) {    
        String hs = "";    
        String stmp = "";    
        for (int i = 0; i < b.length; i++) {    
            stmp = Integer.toHexString(b[i] & 0xFF);    
            if (stmp.length() == 1) {    
                hs += "0" + stmp;    
            }    
            else {    
                hs += stmp;    
            }    
        }    
        return hs.toUpperCase();    
    }   
       
    public static byte[] hex2Byte(String hex) throws IllegalArgumentException {    
        if (hex.length() % 2 != 0) {    
            throw new IllegalArgumentException();    
        }    
        char[] arr = hex.toCharArray();    
        byte[] b = new byte[hex.length() / 2];    
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {    
            String swap = "" + arr[i++] + arr[i];    
            int byteint = Integer.parseInt(swap, 16) & 0xFF;    
            b[j] = new Integer(byteint).byteValue();    
        }    
        return b;    
    }   
    public static byte[] sha1(String str) {
        try {
            return mac("SHA-1", str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    public static byte[] sha2(String str) {
        try {
            return mac("SHA-256", str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    public static byte[] md2(String str) {
        try {
            return mac("MD2", str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    public static byte[] md5(String str) {
        try {
            return mac("MD5", str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    
    public static byte[] mac(String alga, String str) throws NoSuchAlgorithmException  {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance(alga);
                return md.digest(str.getBytes()); 
            } catch (NoSuchAlgorithmException e) {
                throw e;
            }

    }
    

    /**
     * @param bytes source bytes
     * @return base64 encoded bytes
     */
    public static byte[] encodeBase64(byte[] bytes) throws EncoderException {
    	return Base64.encodeBase64(bytes);
    }
    /**
     * Base64 decode
     * 
     * @param bytes   encoded bytes
     * @return original string
     */

    public static byte[] decodeBase64(byte[] bytes) throws DecoderException {
        return Base64.decodeBase64(bytes);
    }
    
     /**
     * URL encode
     * 
     * @param src
     *            original string
     * @return encoded string
     */
    public static String urlEncode(String src) {
        try {
            src = java.net.URLEncoder.encode(src, "UTF-8");

            return src;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return src;
    }

    /**
     * URL decode
     * 
     * @param value
     *            decoded string
     * @return original string
     */
    public static String urlDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return value;
    }

    public static String bin2Dec(byte[] bytes) {
    	BinaryCodec codec = new BinaryCodec();
    	return new String(codec.encode(bytes));
    }
    
    /**
     * @param bytes original 
     * @return hex string
     */
    public static String encodeHex (byte[] bytes) {
        return new String(Hex.encodeHex(bytes));
    }
    
    
    /**
     * @param str hex string
     * @return char bytes
     * @throws DecoderException
     */
    public static byte[] decodeHex (String str) throws DecoderException {
        char[] data = str.toCharArray();
        return Hex.decodeHex(data);
    }
    
    public static String htmlEncode(String str) {
        return StringEscapeUtils.escapeHtml3(str);
    }
    
    public static String htmlDecode(String str) {
        return StringEscapeUtils.unescapeHtml3(str);
    }
    
    public static String xmlEncode(String str) {
        return StringEscapeUtils.escapeXml(str);
    }
    
    public static String xmlDecode(String str) {
        return StringEscapeUtils.unescapeXml(str);
    }
    
    public static String jsEncode(String str) {
        return StringEscapeUtils.escapeJson(str);
    }
    
    public static String jsDecode(String str) {
        return StringEscapeUtils.unescapeJson(str);
    }


    public static String sqlEncode(String str) {
        return StringUtils.replace(str, "'", "''");
    }
    /**
	 * Translates the given String into ASCII code.
	 * 
	 * @param input the input which contains native characters like umlauts etc
	 * @return the input in which native characters are replaced through ASCII code
	 */
	public static String native2ascii( String input ) {
		if (input == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer( input.length() + 60 );
		for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c <= 0x7E) { 
                buffer.append(c);
            }
            else {
            	buffer.append("\\u");
            	String hex = Integer.toHexString(c);
            	for (int j = hex.length(); j < 4; j++ ) {
            		buffer.append( '0' );
            	}
            	buffer.append( hex );
            }
        }
		return buffer.toString();
	}
	
	
	/**
	 * Translates the given String into ASCII code.
	 * 
	 * @param input the input which contains native characters like umlauts etc
	 * @return the input in which native characters are replaced through ASCII code
	 */
	public static String ascii2native( String input ) {
		if (input == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer( input.length() );
		boolean precedingBackslash = false;
		for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (precedingBackslash) {
            	switch (c) {
            	case 'f': c = '\f'; break;
            	case 'n': c = '\n'; break;
            	case 'r': c = '\r'; break;
            	case 't': c = '\t'; break;
            	case 'u':
            		String hex = input.substring( i + 1, i + 5 );
            		c = (char) Integer.parseInt(hex, 16 );
            		i += 4;
            	}
            	precedingBackslash = false;
            } else {
            	precedingBackslash = (c == '\\');
            }
            if (!precedingBackslash) {
                buffer.append(c);
            }
        }
		return buffer.toString();
	}


    /**
     * simple test codes
     * 
     * @param args
     *            no
     */
    public static void main(String[] args) {
    	String seq = "123456789123";
    	long num = Long.parseLong(seq);
    	System.out.println(num + "," + Long.toBinaryString(num));
    	num = num>>>16;
    	System.out.println(num + "," + Long.toBinaryString(num));
    	
    	
    }

}
