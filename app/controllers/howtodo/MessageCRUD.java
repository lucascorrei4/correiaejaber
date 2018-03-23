package controllers.howtodo;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import controllers.CRUD;
import controllers.Security;
import models.User;
import models.howtodo.Message;
import play.data.validation.Error;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Before;
import util.Utils;

@CRUD.For(models.howtodo.Message.class)
public class MessageCRUD extends CRUD {
	
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

	public static void saveMessage(String json) throws UnsupportedEncodingException {
		User connectedUser = AdminPub.getLoggedUserInstitution().getUser();
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
		message.setInstitutionId(AdminPub.getLoggedUserInstitution().getInstitution().getId());
		message.willBeSaved = true;
		/* Validate object before saving */
		if (!validateObjectToSave(message)) {
			List<Error> errors = validation.errors();
			response = "Favor, preencha todos os campos corretamente!";
			status = "ERROR";
			render("includes/formSaveMessage.html", connectedUser, message, response, status, errors);
		} else {
			message.setPostedAt(Utils.getCurrentDateTime());
			message.setInstitutionId(0l);
			message.merge();
			response = "Muito obrigado pela mensagem!";
			status = "SUCCESS";
			render("includes/formSaveMessage.html", connectedUser, message, response, status);
		}
	}

	private static boolean validateObjectToSave(Message message) {
		validation.clear();
		validation.valid(message);
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
	
	public static void list(int page, String search, String searchFields, String orderBy, String order) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		if (page < 1) {
			page = 1;
		}
		String where = null;
		if (orderBy == null) {
			orderBy = "id";
		}
		if (order == null) {
			order = "DESC";
		}
		List<Model> objects = type.findPage(page, search, searchFields, orderBy, order, where);
		Long count = type.count(search, searchFields, where);
		Long totalCount = type.count(null, null, where);
		try {
			render("howtodo/MessageCRUD/list.html", type, objects, count, totalCount, page, orderBy, order);
		} catch (TemplateNotFoundException e) {
			render("howtodo/MessageCRUD/list.html", type, objects, count, totalCount, page, orderBy, order);
		}
	}
}
