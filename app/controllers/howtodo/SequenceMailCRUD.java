package controllers.howtodo;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import controllers.CRUD;
import controllers.Security;
import models.howtodo.Article;
import models.howtodo.FreePage;
import models.howtodo.Parameter;
import models.howtodo.SellPage;
import models.howtodo.SequenceMail;
import models.howtodo.SequenceMailQueue;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Before;
import util.howtodo.SequenceMailTO;
import util.howtodo.TO;

@CRUD.For(models.howtodo.SequenceMail.class)
public class SequenceMailCRUD extends CRUD {

	@Before
	static void globals() {
		if (AdminPub.getLoggedUserInstitution() == null || AdminPub.getLoggedUserInstitution().getUser() == null) {
			ApplicationPub.index();
		}
		renderArgs.put("poweradmin", "lucascorreiaevangelista@gmail.com".equals(AdminPub.getLoggedUserInstitution().getUser().getEmail()) ? "true" : "false");
		renderArgs.put("logged", AdminPub.getLoggedUserInstitution().getUser().id);
		renderArgs.put("enableUser", Security.enableMenu() ? "true" : "false");
		renderArgs.put("idu", AdminPub.getLoggedUserInstitution().getUser().getId());
		renderArgs.put("id", AdminPub.getLoggedUserInstitution().getInstitution() != null ? AdminPub.getLoggedUserInstitution().getInstitution().getId() : null);
		renderArgs.put("institutionName", AdminPub.getLoggedUserInstitution().getInstitution() != null ? AdminPub.getLoggedUserInstitution().getInstitution().getInstitution() : null);
	}

	public static void list(int page, String search, String searchFields, String orderBy, String order) {
		listAll(0, search, searchFields, orderBy, order);
	}

	public static void listAll(int page, String search, String searchFields, String orderBy, String order) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		if (page < 1) {
			page = 1;
		}
		if (orderBy == null) {
			orderBy = "id";
		}
		if (order == null) {
			order = "DESC";
		}
		List<SequenceMail> objects = SequenceMail.find("order by postedAt, sequence desc group by url, sequence").fetch(20);
		Long count = type.count(search, searchFields, (String) request.args.get("where"));
		Long totalCount = type.count(null, null, (String) request.args.get("where"));
		List<SequenceMailTO> listGroupedUrls = getGroupedUrls(objects);
		try {
			render("howtodo/SequenceMailCRUD/listAll.html", type, objects, count, totalCount, page, orderBy, order, listGroupedUrls);
		} catch (TemplateNotFoundException e) {
			render("howtodo/SequenceMailCRUD/listAll.html", type, objects, count, totalCount, page, orderBy, order, listGroupedUrls);
		}
	}

	private static List<SequenceMailTO> getGroupedUrls(List<SequenceMail> objects) {
		List<TO> urls = getAllPublishedUrls();
		List<SequenceMailTO> listSequenceMailTO = new ArrayList<SequenceMailTO>();
		List<SequenceMail> listSequenceMail = null;
		for (TO url : urls) {
			SequenceMailTO sequenceMailTO = new SequenceMailTO();
			sequenceMailTO.setUrl(url.getValue());
			listSequenceMail = new ArrayList<SequenceMail>();
			for (SequenceMail sequenceMail : objects) {
				if (url.getValue().equals(sequenceMail.getUrl())) {
					listSequenceMail.add(sequenceMail);
				}
			}
			Collections.sort(listSequenceMail, new Comparator<SequenceMail>() {
				public int compare(SequenceMail o1, SequenceMail o2) {
					return o1.getSequence() - o2.getSequence();
				}
			});
			sequenceMailTO.setListSequenceMail(listSequenceMail);
			listSequenceMailTO.add(sequenceMailTO);

		}
		return listSequenceMailTO;
	}

	public static void blank() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		SequenceMail object = (SequenceMail) constructor.newInstance();
		List<TO> urls = getAllPublishedUrls();
		try {
			render("howtodo/SequenceMailCRUD/blank.html", type, object, urls);
		} catch (TemplateNotFoundException e) {
			render("howtodo/SequenceMailCRUD/blank.html", type, object, urls);
		}
	}

	public static void show(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		SequenceMail object = SequenceMail.findById(Long.valueOf(id));
		List<TO> urls = getAllPublishedUrls();
		notFoundIfNull(object);
		try {
			render(type, object, urls);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object, urls);
		}
	}

	private static List<TO> getAllPublishedUrls() {
		List<Article> listArticles = Article.find("isActive = true order by postedAt desc").fetch();
		List<FreePage> listFreePages = FreePage.find("isActive = true order by postedAt desc").fetch();
		List<SellPage> listSellPages = SellPage.find("isActive = true order by postedAt desc").fetch();
		List<TO> listTO = new ArrayList<TO>();
		TO to = new TO();
		String siteDomain = Parameter.getCurrentParameter().getSiteDomain();
		to.setLabel("Página inicial - " + siteDomain);
		to.setValue(Parameter.getCurrentParameter().getSiteDomain());
		listTO.add(to);
		for (Article article : listArticles) {
			String url = "/artigos/".concat(String.valueOf(article.id)).concat("/").concat(article.getFriendlyUrl());
			to = new TO();
			to.setLabel(article.getTitleSEO() + " - " + url);
			to.setValue(url);
			listTO.add(to);
		}
		for (FreePage freePage : listFreePages) {
			String url = "/fp/".concat(freePage.getFriendlyUrl());
			to = new TO();
			to.setLabel(freePage.getTitleSEO() + " - " + url);
			to.setValue(url);
			listTO.add(to);
		}
		for (SellPage sellPage : listSellPages) {
			String url = "/pv/".concat(sellPage.getFriendlyUrl());
			to = new TO();
			to.setLabel(sellPage.getTitleSEO() + " - " + url);
			to.setValue(url);
			listTO.add(to);
		}
		return listTO;
	}

	public static void create() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		Model object = (Model) constructor.newInstance();
		Binder.bindBean(params.getRootParamNode(), "object", object);
		validation.valid(object);
		if (validation.hasErrors()) {
			renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/blank.html", type, object);
			} catch (TemplateNotFoundException e) {
				render("CRUD/blank.html", type, object);
			}
		}
		object._save();
		flash.success(play.i18n.Messages.get("crud.created", type.modelName));
		if (params.get("_save") != null) {
			redirect(request.controller + ".listAll");
		}
		if (params.get("_saveAndAddAnother") != null) {
			redirect(request.controller + ".blank");
		}
		redirect(request.controller + ".show", object._key());
	}

	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);
		Binder.bindBean(params.getRootParamNode(), "object", object);
		validation.valid(object);
		if (validation.hasErrors()) {
			renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/show.html", type, object);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		}
		object._save();
		flash.success(play.i18n.Messages.get("crud.saved", type.modelName));
		if (params.get("_save") != null) {
			redirect(request.controller + ".listAll");
		}
		redirect(request.controller + ".show", object._key());
	}

	public static void remove(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		List<SequenceMailQueue> objectQueue = SequenceMailQueue.find("sequenceMail_id = " + id).fetch();
		for (SequenceMailQueue sequenceMailQueue : objectQueue) {
			sequenceMailQueue.delete();
		}
		SequenceMail object = SequenceMail.find("id = " + id + " and institutionId = " + AdminPub.getLoggedUserInstitution().getInstitution().getId()).first();
		notFoundIfNull(object);
		try {
			object.delete();
		} catch (Exception e) {
			flash.error(play.i18n.Messages.get("crud.delete.error", type.modelName));
			redirect(request.controller + ".show", object._key());
		}
		flash.success(play.i18n.Messages.get("crud.deleted", type.modelName));
		redirect(request.controller + ".listAll");
	}

}
