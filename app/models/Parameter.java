package models;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.ivy.Main;

import com.mysql.jdbc.Util;

import controllers.Admin;
import controllers.CRUD.Hidden;
import play.data.binding.As;
import play.data.validation.Required;
import play.db.jpa.Model;
import util.StatusEnum;
import util.StatusInvoiceEnum;
import util.StatusPaymentEnum;
import util.Utils;

@Entity
public class Parameter extends Model {
	
	public String siteName;
	public String siteDomain;
	public String siteMail;
	
	public String siteTitle;
	public String siteDescription;
	public String siteKeywords;

	public String siteFacebook;
	public String siteTwitter;
	public String siteTwitterShort;
	public String siteInstagram;
	public String siteInstagramShort;

	public String siteLogo;
	public String siteHeader;
	public String siteSlogan;

	public String siteBackGroundColor;
	public String siteMainTitleColor;
	
	public String siteIconFontAwesome;
	
	@Lob
	public String logoUrl;

	@Lob
	public String tweet;
	
	public String googleAnalyticsId;
	
	public String googleRemarketingId;

	public String toString() {
		return "Portal: " + siteName;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	
	public String getTweet() {
		return tweet;
	}

	public void setTweet(String tweet) {
		this.tweet = tweet;
	}

	public String getGoogleAnalyticsId() {
		return googleAnalyticsId;
	}

	public void setGoogleAnalyticsId(String googleAnalyticsId) {
		this.googleAnalyticsId = googleAnalyticsId;
	}

	public String getGoogleRemarketingId() {
		return googleRemarketingId;
	}

	public void setGoogleRemarketingId(String googleRemarketingId) {
		this.googleRemarketingId = googleRemarketingId;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteDomain() {
		return siteDomain;
	}

	public void setSiteDomain(String siteDomain) {
		this.siteDomain = siteDomain;
	}

	public String getSiteMail() {
		return siteMail;
	}

	public void setSiteMail(String siteMail) {
		this.siteMail = siteMail;
	}

	public String getSiteTitle() {
		return siteTitle;
	}

	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}

	public String getSiteDescription() {
		return siteDescription;
	}

	public void setSiteDescription(String siteDescription) {
		this.siteDescription = siteDescription;
	}

	public String getSiteKeywords() {
		return siteKeywords;
	}

	public void setSiteKeywords(String siteKeywords) {
		this.siteKeywords = siteKeywords;
	}

	public String getSiteFacebook() {
		return siteFacebook;
	}

	public void setSiteFacebook(String siteFacebook) {
		this.siteFacebook = siteFacebook;
	}

	public String getSiteTwitter() {
		return siteTwitter;
	}

	public void setSiteTwitter(String siteTwitter) {
		this.siteTwitter = siteTwitter;
	}

	public String getSiteTwitterShort() {
		return siteTwitterShort;
	}

	public void setSiteTwitterShort(String siteTwitterShort) {
		this.siteTwitterShort = siteTwitterShort;
	}

	public String getSiteLogo() {
		return siteLogo;
	}

	public void setSiteLogo(String siteLogo) {
		this.siteLogo = siteLogo;
	}

	public String getSiteHeader() {
		return siteHeader;
	}

	public void setSiteHeader(String siteHeader) {
		this.siteHeader = siteHeader;
	}

	public String getSiteSlogan() {
		return siteSlogan;
	}

	public void setSiteSlogan(String siteSlogan) {
		this.siteSlogan = siteSlogan;
	}

	public String getSiteInstagram() {
		return siteInstagram;
	}

	public void setSiteInstagram(String siteInstagram) {
		this.siteInstagram = siteInstagram;
	}

	public String getSiteInstagramShort() {
		return siteInstagramShort;
	}

	public void setSiteInstagramShort(String siteInstagramShort) {
		this.siteInstagramShort = siteInstagramShort;
	}

	public String getSiteBackGroundColor() {
		return siteBackGroundColor;
	}

	public void setSiteBackGroundColor(String siteBackGroundColor) {
		this.siteBackGroundColor = siteBackGroundColor;
	}

	public String getSiteMainTitleColor() {
		return siteMainTitleColor;
	}

	public void setSiteMainTitleColor(String siteMainTitleColor) {
		this.siteMainTitleColor = siteMainTitleColor;
	}

	public String getSiteIconFontAwesome() {
		return siteIconFontAwesome;
	}

	public void setSiteIconFontAwesome(String siteIconFontAwesome) {
		this.siteIconFontAwesome = siteIconFontAwesome;
	}


}
