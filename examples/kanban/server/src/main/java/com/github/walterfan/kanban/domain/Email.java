package com.github.walterfan.kanban.domain;



import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;


/**
 * class: Email
 *
 * @see javax mail API
 */
public class Email implements Cloneable {

    private static Log logger = LogFactory.getLog(Email.class);
    /** DOCUMENT ME! */
    private InternetAddress[] toAddress;

    /** DOCUMENT ME! */
    private InternetAddress[] ccAddress;

    /** DOCUMENT ME! */
    private InternetAddress[] bccAddress;

    /** DOCUMENT ME! */
    private InternetAddress[] replyAddress;

    /** DOCUMENT ME! */
    private InternetAddress fromAddress;

    /** DOCUMENT ME! */
    private String subject = "";

    /** DOCUMENT ME! */
    private String content = "";

    /** DOCUMENT ME! */
    private String contentType = "text/html;charset=utf-8";
    
    public Email() {
        
    }  

    /*
     * @param    toemail    which email you want to send
     * @param    fromemail    your email
     * @param    email subject
     * @param    email content
     */
/**
     * Creates a new Email object.
     *
     * @param to DOCUMENT ME!
     * @param from DOCUMENT ME!
     * @param subject DOCUMENT ME!
     * @param content DOCUMENT ME!
 * @throws AddressException 
     */
    public Email(String to, String from, String subject, String content) throws AddressException {
        setToAddr(to);
        setFromAddr(from);
        this.subject = subject;
        this.content = content;
    }



    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    

    /**
     * DOCUMENT ME!
     *
     * @param from DOCUMENT ME!
     */
    public void setFromAddr(String from) throws AddressException {
        if(StringUtils.isBlank(from)){
            return;
        }
        this.fromAddress = new InternetAddress(from);
        
    }

    public void setFromAddr(String from, String name) throws AddressException {
        if(StringUtils.isBlank(from)){
            return;
        }
        this.fromAddress = new InternetAddress(from);
        if(StringUtils.isBlank(name)) {
            return;
        }
        try {
            this.fromAddress.setPersonal(name);
        } catch (UnsupportedEncodingException e) {
            logger.error("setFromAddr, setPersonal error", e);
        }
}
    
    /**
     * DOCUMENT ME!
     *
     * @param cont DOCUMENT ME!
     */
    public void setContent(String cont) {
        this.content = cont;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getContent() {
        return content;
    }

    /**
     * DOCUMENT ME!
     *
     * @param regex DOCUMENT ME!
     * @param var DOCUMENT ME!
     */
    public void replaceContent(String regex, String var) {
        if (StringUtils.isEmpty(regex) || var == null) {
            return;
        }
        var = escapeSlashes(var, "$");
        this.content = this.content.replaceAll(regex, var);
    }

    public static String escapeSlashes(String strtmp, String letter) {
        if ((strtmp == null) || (letter == null)) {
            return strtmp;
        }

        String strret = "";
        int d = 0;

        while (d >= 0) {
            d = strtmp.indexOf(letter);

            if (d < 0) {
                strret += strtmp;

                break;
            }

            strret += (strtmp.substring(0, d) + "\\" + letter);

            if ((d + 1) > (strtmp.length() - 1)) {
                break;
            }

            strtmp = strtmp.substring(d + 1);
        }

        return strret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sub DOCUMENT ME!
     */
    public void setSubject(String sub) {
        this.subject = sub;
    }

    /**
     * DOCUMENT ME!
     *
     * @param regex DOCUMENT ME!
     * @param var DOCUMENT ME!
     */
    public void replaceSubject(String regex, String var) {
        var = escapeSlashes(var, "$");
        this.subject = this.subject.replaceAll(regex, var);
    }


    public void setBccAddr(String bcc) throws AddressException {
        if(StringUtils.isBlank(bcc)){
            return;
        }
        bcc = StringUtils.replaceChars(bcc, ';', ',');
        this.bccAddress = InternetAddress.parse(bcc);
        
    }


    public void setCcAddr(String cc) throws AddressException {
        if(StringUtils.isBlank(cc)){
            return;
        }
        cc = StringUtils.replaceChars(cc, ';', ',');
        this.ccAddress = InternetAddress.parse(cc);

    }


    public void setToAddr(String to) throws AddressException {
        if(StringUtils.isBlank(to)){
            return;
        }
        to = StringUtils.replaceChars(to, ';', ',');
        this.toAddress = InternetAddress.parse(to);

    }
    
    public void setReplyAddr(String reply) throws AddressException {
        if(StringUtils.isBlank(reply)){
            return;
        }
        reply = StringUtils.replaceChars(reply, ';', ',');
        this.replyAddress = InternetAddress.parse(reply);

    }
    
    public InternetAddress[] getToAddress() {
        return this.toAddress;
    }
   
    public InternetAddress[] getCcAddress() {
        return this.ccAddress;
    }
    
    public InternetAddress[] getBccAddress() {
        return this.bccAddress;
    }

    public InternetAddress getFromAddress() {
        return this.fromAddress;
    }
    
    public InternetAddress[] getReplyAddress() {
        return this.replyAddress;
    }
    
    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }




    
    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }




    
    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        if(toAddress!=null && toAddress.length>0){
            for (int i = 0; i < toAddress.length; i++) {
                sb.append(toAddress[i] + ",");
            }
        }
        return "Email: fromAddress=" + fromAddress  + ", toAddress=" + sb.toString() 
        + "\nsubject=" + subject + ", content=" + content;
    }
   
} //end class
