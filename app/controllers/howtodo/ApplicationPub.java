package controllers.howtodo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controllers.CRUD.ObjectType;
import controllers.Security;
import models.Institution;
import models.MoipNotification;
import models.SendTo;
import models.Sender;
import models.Service;
import models.StatusMail;
import models.User;
import models.howtodo.Article;
import models.howtodo.BodyMail;
import models.howtodo.FreePage;
import models.howtodo.HighlightProduct;
import models.howtodo.LeadSearchAnswer;
import models.howtodo.LeadSearchQuestion;
import models.howtodo.MailList;
import models.howtodo.Message;
import models.howtodo.Parameter;
import models.howtodo.SequenceMailQueue;
import models.howtodo.TheSystem;
import play.data.binding.Binder;
import play.data.validation.Error;
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.vfs.VirtualFile;
import util.UserInstitutionParameter;
import util.Utils;
import util.howtodo.FromEnum;
import util.howtodo.MailTemplates;
import util.howtodo.TypeContentPageEnum;

public class ApplicationPub extends Controller {

	public static void cors() {
		Http.Header hd = new Http.Header();
		hd.name = "Access-Control-Allow-Origin";
		hd.values = new ArrayList<String>();
		hd.values.add("http://localhost:9001");
		Http.Response.current().headers.put("Access-Control-Allow-Origin", hd);
		hd = new Http.Header();
		hd.name = "Access-Control-Allow-Methods";
		hd.values = new ArrayList<String>();
		hd.values.add("POST, GET, PUT, UPDATE, OPTIONS");
		Http.Response.current().headers.put("Access-Control-Allow-Methods", hd);
		hd = new Http.Header();
		hd.name = "Access-Control-Allow-Headers";
		hd.values = new ArrayList<String>();
		hd.values.add("Content-Type, Accept, X-Requested-With");
		Http.Response.current().headers.put("Access-Control-Allow-Headers", hd);
	}

	public static void index() {
		Parameter parameter = Parameter.all().first();
		if (parameter.getFreePageIndex() != null) {
			FreePage freePage = parameter.getFreePageIndex();
			String title = Utils.removeHTML(freePage.getMainTitle());
			String headline = Utils.removeHTML(freePage.getSubtitle1());
			/* Verify if test A/B of Video or Text is enabled. */
			/*
			 * If yes, record a cookie in user navigator to guarantee that he
			 * will see only one page.
			 */
			if (freePage.isAbTestVideoOfText()) {
				Cookie cookie = Http.Request.current().cookies.get("acp_viewed");
				if (cookie == null) {
					if (freePage.isAlternateVideoText()) {
						freePage.setAlternateVideoText(false);
						response.setCookie("acp_viewed", "video");
					} else {
						freePage.setAlternateVideoText(true);
						response.setCookie("acp_viewed", "text");
					}
					freePage.save();
				} else if ("text".equals(cookie.value.toString())) {
					freePage.setAlternateVideoText(true);
				} else if ("video".equals(cookie.value.toString())) {
					freePage.setAlternateVideoText(false);
				}
			}
			freePage.setDescription(FreePageController.replacementInclude(freePage.getDescription()));
			freePage.setDescriptionInactivePage(FreePageController.replacementInclude(freePage.getDescriptionInactivePage()));
			freePage.setOptionalDescription(FreePageController.replacementInclude(freePage.getOptionalDescription()));
			freePage.setSubtitle1(FreePageController.replacementInclude(freePage.getSubtitle1()));
			render("howtodo/FreePageController/details.html", freePage, parameter, title, headline);
		} else {
			List<Article> listArticles = Article.find("isActive = true order by postedAt desc").fetch(4);
			List<Article> listArticles12 = listArticles.subList(0, 2);
			List<Article> listArticles34 = listArticles.subList(2, listArticles.size());
			List<TheSystem> listTheSystems = TheSystem.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
			TheSystem theSystem = new TheSystem();
			theSystem.setShowTopMenu(true);
			render(listTheSystems, listArticles12, listArticles34, parameter);
		}
	}

	public static void generateServiceCode() {
		render();
	}

	public static void newAccount() {
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		List<Article> listArticles = Article.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		List<Article> bottomNews = listArticles.subList(0, 3);
		render(theSystem, bottomNews);
	}

	private static boolean validatePassword(String password, String confirmationPassword) {
		boolean ret = false;
		if (!Utils.isNullOrEmpty(password) && !Utils.isNullOrEmpty(confirmationPassword)) {
			if (password.equals(confirmationPassword))
				ret = true;
		}
		return ret;
	}

	public static void howItWorks() {

	}

	public static int moipResponse() throws UnsupportedEncodingException {
		int httpResponse = HttpServletResponse.SC_PAYMENT_REQUIRED;
		String[] fields = request.params.data.get("body");
		String decodedFields = URLDecoder.decode(fields[0], "UTF-8");
		Gson gson = new GsonBuilder().create();
		/* Parse form content to JSON element */
		MoipNotification moipNotification = new MoipNotification();
		moipNotification = gson.fromJson(decodedFields, MoipNotification.class);
		if (moipNotification != null && (1 == moipNotification.getStatus_pagamento())) {
			String model = moipNotification.getId_transacao().substring(0, 2);
			if ("CCHFEM".equals(model) || "CCHMAS".equals(model)) {
				Service service = Service.find("orderCode = " + moipNotification.getId_transacao()).first();
				httpResponse = updatePayment(service, moipNotification);
			}
		}
		return httpResponse;
	}

	private static int updatePayment(Object object, MoipNotification moipNotification) {
		if (object instanceof Service) {
			Service service = (Service) object;
			// service.setPaymentForm(String.valueOf(moipNotification.getForma_pagamento()));
			// service.setPaymentType(moipNotification.getTipo_pagamento());
			service.merge();
			return HttpServletResponse.SC_OK;
		}
		return HttpServletResponse.SC_PAYMENT_REQUIRED;
	}

	public static void saveNewAccount(@Valid User user, String confirmPassword) {
		if (user.getName() != null) {
			if (!validateForm(user, confirmPassword)) {
				user.password = "";
				confirmPassword = "";
				render("@newAccount", user);
				return;
			} else {
				user.setAdmin(true);
				user.setActive(true);
				user.setPostedAt(Utils.getCurrentDateTime());
				user.setInstitutionId(0);
				user.save();
				flash.clear();
				validation.errors().clear();
				flash.success("Cadastro realizado com sucesso! Você está quase lá, " + user.getName() + "! Para entrar, preencha os campos abaixo. :)", "");
				redirect("/login");
			}
		}
		render("@newAccount");
	}

	private static boolean validateForm(User user, String confirmPassword) {
		boolean ret = false;
		validation.required(user.getName()).message("Favor, insira o seu nome.").key("user.name");
		validation.required(user.getLastName()).message("Favor, insira o seu sobrenome.").key("user.lastName");
		validation.required(user.getEmail()).message("Favor, insira o seu e-mail.").key("user.email");
		validation.email(user.getEmail()).message("Favor, insira o seu e-mail no formato nome@provedor.com.br.").key("user.email");
		validation.isTrue(User.verifyByEmail(user.getEmail()) == null).message("Já existe um usuário com este e-mail.").key("user.email");
		validation.required(user.getPassword()).message("Favor, insira uma senha.").key("user.password");
		validation.required(confirmPassword).message("Favor, digite novamente a senha.").key("confirmPassword");
		validation.isTrue(validatePassword(user.getPassword(), confirmPassword)).message("As senhas digitadas devem ser iguais.").key("confirmPassword");
		params.flash();
		validation.keep();
		if (!validation.hasErrors())
			ret = true;
		else {
			for (play.data.validation.Error error : validation.errors()) {
				System.out.println(error.getKey() + " " + error.message());
			}
		}
		return ret;
	}

	public static void saveQuickAccount(String json) throws UnsupportedEncodingException {
		String body = params.get("body", String.class);
		String decodedParams = URLDecoder.decode(body, "UTF-8");
		String[] bodyParam = decodedParams.split("&");
		String name = Utils.getValueFromUrlParam(bodyParam[0]);
		String lastName = Utils.getValueFromUrlParam(bodyParam[1]);
		String phone = Utils.getValueFromUrlParam(bodyParam[2]);
		String mail = Utils.getValueFromUrlParam(bodyParam[3]);
		String password = Utils.getValueFromUrlParam(bodyParam[4]);
		String repeatePassword = Utils.getValueFromUrlParam(bodyParam[5]);
		String responseQuickAccount = "";
		String statusQuickAccount = "";
		/* Get body content from client request */
		User user = new User();
		user.id = 0l;
		user.setName(name);
		user.setLastName(lastName);
		user.setPhone1(phone);
		user.setEmail(mail);
		user.setPassword(password);
		user.setRepeatPassword(repeatePassword);
		user.willBeSaved = true;
		/* Validate object before saving */
		if (!validateForm(user)) {
			responseQuickAccount = "Favor, preencha todos os campos corretamente!";
			statusQuickAccount = "ERROR";
			render("includes/newQuickAccount.html", user, responseQuickAccount, statusQuickAccount);
		} else if (user != null && !validatePassword(user.getPassword(), user.getRepeatPassword())) {
			params.flash();
			validation.keep();
			statusQuickAccount = "ERROR";
			responseQuickAccount = "As senhas que você digitou não são iguais. :(";
			render("includes/newQuickAccount.html", user, responseQuickAccount, statusQuickAccount);
		} else {
			user.setPassword(Utils.encode(user.password));
			user.setAdmin(true);
			user.setPostedAt(Utils.getCurrentDateTime());
			user.setInstitutionId(0l);
			user.setActive(true);
			user.setFromMonetizze(false);
			user.merge();
			responseQuickAccount = "Ótimo. Seu cadastro foi criado com sucesso. :)";
			statusQuickAccount = "SUCCESS";
			user = new User();
			render("includes/newQuickAccount.html", user, responseQuickAccount, statusQuickAccount);
		}

	}

	private static boolean validateObjectToSave(User user) {
		validation.clear();
		validation.valid(user);
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

	@SuppressWarnings("unused")
	private static boolean validateForm(User user) {
		boolean ret = false;
		validation.required(user.getName()).message("Favor, insira o seu nome.").key("user.name");
		validation.required(user.getLastName()).message("Favor, insira o seu sobrenome.").key("user.lastName");
		validation.required(user.getEmail()).message("Favor, insira o seu e-mail.").key("user.email");
		validation.email(user.getEmail()).message("Favor, insira o seu e-mail no formato nome@provedor.com.br.").key("user.email");
		validation.isTrue(User.verifyByEmail(user.getEmail()) == null).message("Já existe um usuário com este e-mail.").key("user.email");
		validation.required(user.getPassword()).message("Favor, insira uma senha.").key("user.password");
		validation.required(user.getRepeatPassword()).message("Favor, digite novamente a senha.").key("confirmPassword");
		validation.isTrue(validatePassword(user.getPassword(), user.getRepeatPassword())).message("As senhas digitadas devem ser iguais.").key("confirmPassword");
		params.flash();
		validation.keep();
		if (!validation.hasErrors())
			ret = true;
		else {
			for (play.data.validation.Error error : validation.errors()) {
				System.out.println(error.getKey() + " " + error.message());
			}
		}
		return ret;
	}

	private static boolean validateForm(Institution institution) {
		boolean ret = false;
		validation.required(institution.getInstitution()).message("Favor, insira o nome da Instituição.").key("institution.institution");
		validation.required(institution.getEmail()).message("Favor, insira o e-mail.").key("institution.email");
		validation.email(institution.getEmail()).message("Favor, insira o e-mail no formato nome@provedor.com.br.").key("institution.email");
		validation.required(institution.getLogo()).message("Favor, insira a logomarca.").key("institution.logo");
		validation.isTrue(Institution.verifyByEmail(institution.getEmail()) == null).message("Já existe uma instituição com este e-mail.").key("institution.email");
		// if (!Utils.isNullOrEmpty(institution.getCnpj())) {
		// validation.isTrue(Utils.validateCPFOrCNPJ(institution.getCnpj())).message("CNPJ
		// inválido.")
		// .key("institution.cnpj");
		// validation.isTrue(Institution.verifyByCnpj(institution.getCnpj()) ==
		// null)
		// .message("Já existe uma Instituição com este
		// CNPJ.").key("institution.cnpj");
		// }
		validation.required(institution.getAddress()).message("Favor, digite o endereço.").key("institution.address");
		validation.required(institution.getNeighborhood()).message("Favor, informe o bairro.").key("institution.neighborhood");
		validation.required(institution.getCep()).message("Favor, informe o CEP.").key("institution.cep");
		validation.required(institution.getPhone1()).message("Favor, informe o telefone.").key("institution.phone1");
		params.flash();
		validation.keep();
		if (!validation.hasErrors())
			ret = true;
		else {
			for (play.data.validation.Error error : validation.errors()) {
				System.out.println(error.getKey() + " " + error.message());
			}
		}
		return ret;
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

	public static void followAdmin() {
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		List<Article> listArticles = Article.find("highlight = false and isActive = true order by postedAt desc").fetch(6);
		List<Article> bottomNews = listArticles.subList(2, listArticles.size());
		render(theSystem, bottomNews);
	}

	public static void thankLead() {
		Parameter parameter = Parameter.all().first();
		List<Article> listArticles = Article.find("isActive = true order by postedAt desc").fetch(6);
		List<Article> bottomNews = listArticles.subList(0, 3);
		TheSystem theSystem = new TheSystem();
		theSystem.setShowTopMenu(true);
		render(bottomNews, parameter, theSystem);
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
		if (parameter.getLogo().exists()) {
			renderBinary(parameter.getLogo().get(), "img-logo-" + parameter.id, "image/png", true);
			return;
		}
	}

	public static void getIcon() {
		Parameter parameter = getCurrentParameter();
		if (parameter.getIcon().exists()) {
			renderBinary(parameter.getIcon().get());
			return;
		}
	}

	public static void getHomeBackgroundImage() {
		Parameter parameter = getCurrentParameter();
		if (parameter.getHomeBackgroundImage().exists()) {
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

	public static void main(String[] args) {
		String url = "http://localhost:9002/?src=from-adw&utm_campaing=since-20180302";
		System.out.println(url.split("\\?")[0]);
	}

	public static void saveMailList() throws UnsupportedEncodingException {
		String resp = null;
		String status = null;
		String body = params.get("body", String.class);
		String decodedParams = URLDecoder.decode(body, "UTF-8");
		String phone = params.get("phone");
		String[] params = decodedParams.split("&");
		String name = Utils.getValueFromUrlParam(params[0]);
		String mail = Utils.getValueFromUrlParam(params[1]);
		String origin = Utils.getValueFromUrlParam(params[2]);
		String typeContentPage = Utils.getValueFromUrlParam(params[3]);
		String url = Utils.splitUrlStringFromParameters(Utils.getAllValueParamByKey(params, "url"), 0);
		String urlParameters = Utils.splitUrlStringFromParameters(Utils.getAllValueParamByKey(params, "url"), 1);
		/* Verify if lead has been registered in current capture page */
		url = (url.charAt(url.length() - 1) == '/' ? (url.substring(0, url.length() - 1)) : url);
		MailList mailList = MailList.find("mail = '" + mail + "' and url = '" + url + "'").first();
		if (mailList == null) {
			mailList = new MailList();
			mailList.id = 0l;
		}
		if (Utils.isNullOrEmpty(name)) {
			mailList.setName("Oi");
		} else {
			mailList.setName(name);
		}
		mailList.setName(name);
		mailList.setMail(mail);
		mailList.setPhone(phone);
		mailList.origin = FromEnum.getNameByValue(origin);
		mailList.setUrl(url);
		mailList.setUrlParameters(urlParameters);
		/*
		 * Verify if the page has A/B Test of Text or Video. If not, set as not
		 * defined
		 */
		if ("true".equals(typeContentPage)) {
			mailList.setTypeContentPage(TypeContentPageEnum.TextContent);
		} else if ("false".equals(typeContentPage)) {
			mailList.setTypeContentPage(TypeContentPageEnum.VideoContent);
		} else if ("nd".equals(typeContentPage) || Utils.isNullOrEmpty(typeContentPage)) {
			mailList.setTypeContentPage(TypeContentPageEnum.NotDefined);
		}
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
		String redirectTo = null;
		String partOf = null; 
		String pageParameter = null;
		FreePage freePage = null;
		Parameter parameter = getCurrentParameter();
		if (url.contains("fp/")) {
			partOf = url.substring(url.indexOf("fp/"));
			pageParameter = partOf.split("/")[1];
			freePage = FreePage.findByFriendlyUrl(pageParameter);
			redirectTo = freePage.getRedirectTo();
		} else if (url.equals(parameter.getSiteDomain()) && !Utils.isNullOrEmpty(parameter.getFreePageIndex())) {
			redirectTo = parameter.getEmbedThankLead();
			freePage = parameter.getFreePageIndex();
			redirectTo = parameter.getFreePageIndex().getRedirectTo();
		}
		/* Render page based on origin */
		switch (FromEnum.getNameByValue(origin)) {
		case HomePageTop:
			render("includes/formNewsletterTop.html", status, resp, freePage, redirectTo);
			break;
		case HomePageBottom:
			render("includes/formNewsletterBottom.html", status, resp, freePage, redirectTo);
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
		case NewsletterFreePage:
			render("includes/formNewsLetterFreePage.html", status, resp, freePage, redirectTo);
			break;
		case NewsletterFreePageBootstrap:
			render("includes/formNewsLetterFreePageBootstrap.html", status, resp, freePage, redirectTo);
			break;
		}
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
			sender.setCompany(Utils.isNullOrEmpty(parameter.getMailSenderName()) ? parameter.getSiteTitle() : parameter.getMailSenderName());
			sender.setFrom(Utils.isNullOrEmpty(parameter.getMailSenderFrom()) ? parameter.getSiteMail() : parameter.getMailSenderFrom());
			sender.setKey("resetpass");
			/* SendTo object */
			BodyMail bodyMail = new BodyMail();
			bodyMail.setTitle1("Oi, " + user.getName() + "! Veja abaixo:");
			bodyMail.setTitle2(parameter.getSiteTitle() + " - Nova senha");
			bodyMail.setFooter1(parameter.getSiteDomain() + "/nova-senha/" + Utils.encode(user.getEmail()));
			bodyMail.setImage1(parameter.getSiteDomain() + "/logo");
			bodyMail.setBodyHTML(MailTemplates.getHTMLTemplateResetPass(bodyMail, parameter));
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
			ApplicationPub.index();
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

	public static void leadSearch(String campaing, String lead) {
		boolean pageNotAvailable = true;
		Parameter parameter = getCurrentParameter();
		if (Utils.isNullOrEmpty(campaing)) {
			render(pageNotAvailable, parameter);
		} else if (Utils.isNullOrEmpty(lead)) {
			render(pageNotAvailable, parameter);
		} else {
			pageNotAvailable = false;
			LeadSearchQuestion question = LeadSearchQuestion.find("campaing = '" + campaing + "'").first();
			String leadDecoded = Utils.decode(lead);
			MailList mailList = MailList.verifyByEmail(leadDecoded);
			render(pageNotAvailable, parameter, question, mailList);
		}
	}

	public static void sendAnswer() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Parameter parameter = getCurrentParameter();
		ObjectType type = ObjectType.get(LeadSearchAnswerCRUD.class);
		notFoundIfNull(type);
		Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		LeadSearchAnswer leadSearchAnswer = new LeadSearchAnswer();
		leadSearchAnswer = (LeadSearchAnswer) constructor.newInstance();
		Binder.bindBean(params.getRootParamNode(), "object", leadSearchAnswer);
		String campaing = params.get("object.leadSearchQuestion");
		String message = null;
		boolean pageNotAvailable = false;
		MailList mailList = MailList.verifyByEmail(params.get("object.mailList"));
		LeadSearchQuestion question = (LeadSearchQuestion) LeadSearchQuestion.find("campaing = '" + campaing + "'").first();
		validateSearchFields(pageNotAvailable, parameter, question, mailList, message, leadSearchAnswer);
		LeadSearchAnswer answer = getAnswerByQuestionLeadInstitutionId(question.id, mailList.id, question.institutionId);
		if (answer == null) {
			leadSearchAnswer.id = 0l;
		} else {
			leadSearchAnswer.id = answer.id;
		}
		leadSearchAnswer.setLeadSearchQuestion(question);
		leadSearchAnswer.setInstitutionId(question.getInstitutionId());
		leadSearchAnswer.setMailList(mailList);
		leadSearchAnswer.setPostedAt(Utils.getCurrentDateTime());
		leadSearchAnswer.setActive(true);
		leadSearchAnswer.setTestimonial("");
		leadSearchAnswer.willBeSaved = true;
		leadSearchAnswer.merge();
		answer = getAnswerByQuestionLeadInstitutionId(question.id, mailList.id, question.institutionId);
		boolean showThanksMessage = false;
		render("@leadSearchThanks", question, answer, parameter, mailList, showThanksMessage);
	}

	private static LeadSearchAnswer getAnswerByQuestionLeadInstitutionId(Long questionId, Long leadId, Long institutionId) {
		return (LeadSearchAnswer) LeadSearchAnswer.find("question_id = " + questionId + " and lead_id = " + leadId + " and institutionId = " + institutionId).first();
	}

	private static void validateSearchFields(boolean pageNotAvailable, Parameter parameter, LeadSearchQuestion question, MailList mailList, String message, LeadSearchAnswer leadSearchAnswer) {
		boolean error = false;
		if (Utils.isNullOrEmpty(leadSearchAnswer.getBusiness())) {
			error = true;
			message = "Por favor, informe o negócio que você trabalha.";
		} else if (Utils.isNullOrEmpty(leadSearchAnswer.getProfession())) {
			error = true;
			message = "Por favor, informe sua profissão.";
		} else if (Utils.isNullOrEmpty(leadSearchAnswer.getAnswer1())) {
			error = true;
			message = "Por favor, responda a questão #2.";
		} else if (Utils.isNullOrEmpty(leadSearchAnswer.getAnswer2())) {
			error = true;
			message = "Por favor, responda a questão #3.";
		} else if (Utils.isNullOrEmpty(leadSearchAnswer.getAnswer3())) {
			error = true;
			message = "Por favor, responda a questão #4.";
		} else if (Utils.isNullOrEmpty(leadSearchAnswer.getAnswer4())) {
			error = true;
			message = "Por favor, responda a questão #5.";
		} else if (Utils.isNullOrEmpty(leadSearchAnswer.getAnswer5())) {
			error = true;
			message = "Por favor, responda a questão #6.";
		}
		if (error) {
			render("@leadSearch", pageNotAvailable, parameter, question, mailList, message, leadSearchAnswer);
			return;
		}
	}

	public static void leadSearchThanks(LeadSearchQuestion question, LeadSearchAnswer answer) {
		render(question, answer);
	}

	public static void saveTestimonial() {
		Parameter parameter = getCurrentParameter();
		boolean showThanksMessage = true;
		String answer = params.get("ansid");
		String testimonial = params.get("object.testimonial");
		boolean authorize = params.get("object.isAuthorize", Boolean.class);
		LeadSearchAnswer leadSearchAnswer = LeadSearchAnswer.findById(Long.valueOf(answer));
		leadSearchAnswer.setTestimonial(testimonial);
		leadSearchAnswer.setAuthorize(authorize);
		leadSearchAnswer.save();
		render("@leadSearchThanks", showThanksMessage, parameter);
	}

}
