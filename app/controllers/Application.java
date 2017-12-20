package controllers;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import models.Article;
import models.BodyMail;
import models.HighlightProduct;
import models.MailList;
import models.Message;
import models.Parameter;
import models.SendTo;
import models.Sender;
import models.SequenceMailQueue;
import models.StatusMail;
import models.TheSystem;
import models.User;
import play.data.validation.Error;
import play.mvc.Controller;
import play.vfs.VirtualFile;
import util.FromEnum;
import util.MailTemplates;
import util.TypeAdsNewsEnum;
import util.Utils;

public class Application extends Controller {
	
	static Parameter currentParameter;

	public static void index() {
		List<Article> highlightArticles = Article.find("highlight = true and isActive = true").fetch(2);
		List<Article> listArticles = Article.find("highlight = false and isActive = true order by postedAt desc").fetch();
		List<Article> sidebarRightNews = getArticlesSidebarRightNews(listArticles);
		List<Article> bottomNews = getArticlesBottomNews(listArticles, sidebarRightNews);
		/* Article Ads */
		Article articleTopAds = getArticleAdsTop(listArticles);
		List<Article> articleSidebarRightAds = getArticleAdsSidebarRight(listArticles);
		List<Article> articleBottomAds = getArticlesAdsBottom(listArticles);
		Parameter parameter = getCurrentParameter();
		List<TheSystem> listTheSystems = TheSystem.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		List<HighlightProduct> listHightlightProduct = HighlightProduct.find("isActive = true order by postedAt desc").fetch();
		render(bottomNews, sidebarRightNews, highlightArticles, parameter, listTheSystems, theSystem, articleTopAds, articleSidebarRightAds, articleBottomAds, listHightlightProduct);
	}

	public static void details(String id) {
		Article article = Article.findById(Long.valueOf(id));
		List<Article> listArticles = Article.find("highlight = false and isActive = true and id <>  " + Long.valueOf(id) + " order by postedAt desc").fetch();
		List<Article> sidebarRightNews = getArticlesSidebarRightNews(listArticles);
		List<Article> bottomNews = getArticlesBottomNews(listArticles, sidebarRightNews);
		/* Article Ads */
		Article articleTopAds = getArticleAdsTop(listArticles);
		List<Article> articleSidebarRightAds = getArticleAdsSidebarRight(listArticles);
		List<Article> articleBottomAds = getArticlesAdsBottom(listArticles);
		Parameter parameter = getCurrentParameter();
		List<TheSystem> listTheSystems = TheSystem.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		String title = Utils.removeHTML(article.getTitle());
		HighlightProduct hightlightProduct = HighlightProduct.find("isActive = true and isHighlight = true").first();
		render(article, bottomNews, sidebarRightNews, parameter, listTheSystems, theSystem, title, articleTopAds, articleSidebarRightAds, articleBottomAds, hightlightProduct);
	}

	private static List<Article> getArticlesSidebarRightNews(List<Article> listArticles) {
		List<Article> listArticle = new ArrayList<Article>();
		for (Article article : listArticles) {
			if (article.getTypeAds() != null && TypeAdsNewsEnum.isDefaultArticle(article.getTypeAds().getValue())) {
				listArticle.add(article);
			}
			if (listArticle.size() == 2) {
				return listArticle;
			}
		}
		return listArticle;
	}

	private static List<Article> getArticlesBottomNews(List<Article> listArticles, List<Article> listArticlesSidebarRight) {
		List<Article> listArticle = new ArrayList<Article>();
		for (Article article : listArticles) {
			if (article.getTypeAds() != null && TypeAdsNewsEnum.isDefaultArticle(article.getTypeAds().getValue()) && !listArticlesSidebarRight.contains(article)) {
				listArticle.add(article);
			}
			if (listArticle.size() == 6) {
				return listArticle;
			}
		}
		return listArticle;
	}

	private static List<Article> getArticlesAdsBottom(List<Article> listArticles) {
		List<Article> listArticle = new ArrayList<Article>();
		for (Article article : listArticles) {
			if (article.getTypeAds() != null && TypeAdsNewsEnum.isAdsBottom(article.getTypeAds().getValue())) {
				listArticle.add(article);
			}
			if (listArticle.size() == 4) {
				return listArticle;
			}
		}
		return listArticle;
	}

	private static Article getArticleAdsTop(List<Article> listArticles) {
		for (Article article : listArticles) {
			if (article.getTypeAds() != null && TypeAdsNewsEnum.isAdsTop(article.getTypeAds().getValue())) {
				return article;
			}
		}
		return null;
	}

	private static List<Article> getArticleAdsSidebarRight(List<Article> listArticles) {
		List<Article> listArticle = new ArrayList<Article>();
		for (Article article : listArticles) {
			if (article.getTypeAds() != null && TypeAdsNewsEnum.isAdsSidebarRight(article.getTypeAds().getValue())) {
				listArticle.add(article);
			}
			if (listArticle.size() == 2) {
				return listArticle;
			}
		}
		return listArticle;
	}

	public static void getImage(long id, String index) {
		final Article article = Article.findById(id);
		notFoundIfNull(article);
		if ("1".equals(index)) {
			if (article.getImage1() != null) {
				renderBinary(article.getImage1().get(), index.concat("-").concat(article.friendlyUrl));
				return;
			}
		} else if ("2".equals(index)) {
			if (article.getImage2() != null) {
				renderBinary(article.getImage2().get());
				return;
			}
		} else if ("3".equals(index)) {
			if (article.getImage3() != null) {
				renderBinary(article.getImage3().get());
				return;
			}
		} else if ("4".equals(index)) {
			if (article.getImage4() != null) {
				renderBinary(article.getImage4().get());
				return;
			}
		} else if ("5".equals(index)) {
			if (article.getImage5() != null) {
				renderBinary(article.getImage5().get());
				return;
			}
		}
	}

	public static void getImageHighlightProduct(long id) {
		final HighlightProduct hightlightProduct = HighlightProduct.findById(id);
		notFoundIfNull(hightlightProduct);
		if (hightlightProduct.getImage() != null) {
			renderBinary(hightlightProduct.getImage().get());
			return;
		}
	}

	public static void getLogo() {
		Parameter parameter = getCurrentParameter();
		if (parameter.getLogo() != null) {
			renderBinary(parameter.getLogo().get());
			return;
		}
	}

	public static void getHomeBackgroundImage() {
		Parameter parameter = getCurrentParameter();
		if (parameter.getHomeBackgroundImage() != null) {
			renderBinary(parameter.getHomeBackgroundImage().get());
			return;
		}
	}

	/* Pixel tracking e-mail */
	public static void hrpx(long id) {
		VirtualFile vf = VirtualFile.fromRelativePath("/public/images/hr-line-gray.png");
		File f = vf.getRealFile();
		SequenceMailQueue seqMail = SequenceMailQueue.findById(id);
		seqMail.setMailRead(true);
		seqMail.save();
		renderBinary(f);
	}

	public static void contact() {
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		List<Article> listArticles = Article.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		List<Article> bottomNews = listArticles.subList(0, 3);
		Parameter parameter = getCurrentParameter();
		render(theSystem, bottomNews, parameter);
	}

	public static void about() {
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		List<Article> listArticles = Article.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		List<Article> bottomNews = listArticles.subList(0, 3);
		Parameter parameter = getCurrentParameter();
		render(theSystem, bottomNews, parameter);
	}

	public static void privacyPolicy() {
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		List<Article> listArticles = Article.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		List<Article> bottomNews = listArticles.subList(0, 3);
		Parameter parameter = getCurrentParameter();
		render(theSystem, bottomNews, parameter);
	}

	public static void termsConditions() {
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		List<Article> listArticles = Article.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		List<Article> bottomNews = listArticles.subList(0, 3);
		Parameter parameter = getCurrentParameter();
		render(theSystem, bottomNews, parameter);
	}
	
	public static void saveMailList() throws UnsupportedEncodingException {
		String resp = null;
		String status = null;
		String body = params.get("body", String.class);
		String decodedParams = URLDecoder.decode(body, "UTF-8");
		String[] params = decodedParams.split("&");
		String name = Utils.getValueFromUrlParam(params[0]);
		String mail = Utils.getValueFromUrlParam(params[1]);
		String origin = Utils.getValueFromUrlParam(params[2]);
		String url = Utils.getValueFromUrlParam(params[3]);
		MailList mailList = new MailList();
		mailList.id = 0l;
		if (Utils.isNullOrEmpty(name)) {
			mailList.setName("");
		} else {
			mailList.setName(name);
		}
		mailList.setName(name);
		mailList.setMail(mail);
		mailList.origin = FromEnum.getNameByValue(origin);
		mailList.setUrl(url);
		/* Making validations */
		validation.clear();
		validation.valid(mailList);
		validation.email(mailList.getMail()).message("Favor, insira o seu e-mail no formato nome@provedor.com.br.").key("mailList.mail1");
		if (validation.hasErrors()) {
			status = "ERROR";
			resp = "Favor, insira o seu e-mail no formato nome@provedor.com.br.";
		} else {
			status = "SUCCESS";
			resp = "E-mail incluído com sucesso. Gratidão.";
			mailList.setPostedAt(Utils.getCurrentDateTime());
			mailList.merge();
		}

		/* Render page based on origin */
		switch (FromEnum.getNameByValue(origin)) {
		case HomePageTop:
			render("includes/formNewsletterTop.html", status, resp);
			break;
		case HomePageBottom:
			render("includes/formNewsletterBottom.html", status, resp);
			break;
		case NewsPage:
			render("includes/formNewsletterTips.html", status, resp);
			break;
		case CapturePageTop:
			render("includes/formCapturePageTop.html", status, resp);
			break;
		case CapturePageBottom:
			render("includes/formCapturePageBottom.html", status, resp);
			break;
		}
	}
	
	public static void saveMessage(String json) throws UnsupportedEncodingException {
		String response = null;
		String status = null;
		/* Get body content from client request */
		String[] fields = request.params.data.get("body");
		String decodedFields = URLDecoder.decode(fields[0], "UTF-8");
		Gson gson = new GsonBuilder().create();
		/* Parse form content to JSON element */
		String jsonParam = Utils.transformQueryParamToJson(decodedFields, "message.");
		JsonParser parser = new JsonParser();
		JsonObject jsonElement = (JsonObject) parser.parse(jsonParam);
		jsonElement.addProperty("id", Long.valueOf(0));
		/* Save Client */
		/* Create object parsing JSON element */
		Message message = new Message();
		message = gson.fromJson(jsonElement, Message.class);
		message.id = 0l;
		message.willBeSaved = true;
		/* Validate object before saving */
		if (!validateObjectToSave(message)) {
			List<Error> errors = validation.errors();
			response = "Favor, preencha todos os campos corretamente!";
			status = "ERROR";
			render("includes/newQuickContact.html", message, response, status, errors);
		} else {
			message.setPostedAt(Utils.getCurrentDateTime());
			message.setInstitutionId(0l);
			message.merge();
			response = "Muito obrigado pela mensagem!";
			status = "SUCCESS";
			render("includes/newQuickContact.html", message, response, status);
		}
	}
	
	private static boolean validateObjectToSave(Message message) {
		validation.clear();
		validation.valid(message);
		validation.email(message.getMail()).message("Favor, insira o seu e-mail no formato nome@provedor.com.br.").key("message.mail");
		if (validation.hasErrors()) {
			for (play.data.validation.Error e : validation.errors()) {
				System.out.println(e.message());
			}
			params.flash();
			validation.keep();
			return false;
		}
		return true;
	}
	
	private static Parameter getCurrentParameter() {
		return Parameter.all().first();
	}
	
	public static void unsubscribe(String mailLead) {
		Parameter parameter = getCurrentParameter();
		String mail = Utils.decode(mailLead);
		List<MailList> listMail = MailList.find("mail = '" + mail + "'").fetch();
		String lead = listMail.iterator().next().getName();
		for (MailList mL : listMail) {
			mL.setActive(false);
			mL.save();
		}
		render(lead, parameter);
	}
	
	public static void modalPass() throws UnsupportedEncodingException {
		String response = null;
		String status = null;
		String value = params.get("value", String.class);
		String email = Utils.removeAccent(URLDecoder.decode(value, "UTF-8"));
		if (!Utils.isNullOrEmpty(email) && Utils.validateEmail(email) && User.verifyByEmail(email) != null) {
			Parameter parameter = getCurrentParameter();
			User user = User.verifyByEmail(email);
			MailController mailController = new MailController();
			/* SendTo object */
			SendTo sendTo = new SendTo();
			sendTo.setDestination(email);
			sendTo.setName(user.getName());
			sendTo.setSex("");
			sendTo.setStatus(new StatusMail());
			/* Sender object */
			Sender sender = new Sender();
			sender.setCompany("Seu Pedido Online");
			sender.setFrom("contato@seupedido.online");
			sender.setKey("resetpass");
			/* SendTo object */
			BodyMail bodyMail = new BodyMail();
			bodyMail.setTitle1("Oi, " + user.getName() + "! Veja abaixo:");
			bodyMail.setTitle2(parameter.getSiteTitle() + " - Nova senha");
			bodyMail.setFooter1(parameter.getSiteDomain() + "/nova-senha/" + Utils.encode(user.getEmail()));
			bodyMail.setImage1(parameter.getSiteDomain() + "/logo");
			bodyMail.setBodyHTML(MailTemplates.getHTMLTemplateResetPass(bodyMail));
			if (mailController.sendHTMLMail(sendTo, sender, bodyMail, null, parameter)) {
				status = "SUCCESS";
				response = "E-mail enviado com sucesso! Favor, verifique sua caixa de entrada, spam ou lixeira.";
			} else {
				status = "ERROR";
				response = "Houve um problema ao enviar. :(";
			}
		} else {
			status = "ERROR";
			response = "E-mail não encontrado ou digitado incorretamente! :(";
		}
		render(response, status);
	}

	public static void newPass(String id) throws Throwable {
		Parameter parameter = getCurrentParameter();
		String mail = Utils.decode(id);
		User user = User.verifyByEmail(mail);
		if (user == null) {
			Application.index();
		} else {
			render(user, parameter);
		}
	}

	public static void confirmPass() throws UnsupportedEncodingException {
		Parameter parameter = getCurrentParameter();
		String response = null;
		String status = null;
		String body = params.get("body", String.class);
		String[] params = body.split("&");
		String pass1 = Utils.getValueFromUrlParam(params[0]);
		String pass2 = Utils.getValueFromUrlParam(params[1]);
		String ref = Utils.getValueFromUrlParam(params[2]);
		User user = User.verifyByEmail(Utils.decode(Utils.decodeUrl(ref)));
		if (user == null) {
			status = "ERROR";
			response = "Houve um problema. :(";
			render("Application/newPass.html", response, status, user, parameter);
		}
		if (validatePassword(pass1, pass2)) {
			user.setPassword(Utils.encode(pass1));
			user.save();
			status = "SUCCESS";
			response = "Nova senha criada com sucesso. Estamos voltando para a página de login. Ok?";
		} else {
			status = "ERROR";
			response = "As senhas não são iguais. :(";
		}
		render("Application/newPass.html", response, status, user, parameter);
	}
	
	private static boolean validatePassword(String password, String confirmationPassword) {
		boolean ret = false;
		if (!Utils.isNullOrEmpty(password) && !Utils.isNullOrEmpty(confirmationPassword)) {
			if (password.equals(confirmationPassword))
				ret = true;
		}
		return ret;
	}

}

