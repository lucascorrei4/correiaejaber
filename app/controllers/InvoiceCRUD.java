package controllers;

import java.util.List;

import controllers.howtodo.AdminPub;
import controllers.howtodo.ApplicationPub;
import models.Institution;
import models.Invoice;
import models.User;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Before;
import util.UserInstitutionParameter;

@CRUD.For(models.Invoice.class)
public class InvoiceCRUD extends CRUD {
	
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
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		if (page < 1) {
			page = 1;
		}
		String where = "institutionId = " + AdminPub.getLoggedUserInstitution().getInstitution().getId();
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
			render(type, objects, count, totalCount, page, orderBy, order);
		} catch (TemplateNotFoundException e) {
			render("FinancialCRUD/list.html", type, objects, count, totalCount, page, orderBy, order);
		}
	}
	
	public static void show(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		// Filtro pelo usuário conectado para proteger os dados dos demais usuários
		Invoice object = Invoice.find("id = " + id + " and institutionId = " + AdminPub.getLoggedUserInstitution().getInstitution().getId()).first();
		notFoundIfNull(object);
		try {
			render(type, object);
		} catch (TemplateNotFoundException e) {
			render("CRUD/show.html", type, object);
		}
	}
	
	public static void signature(String id) {
		UserInstitutionParameter userInstitutionParameter = AdminPub.getLoggedUserInstitution();
		Institution institution = userInstitutionParameter.getInstitution();
		User user = userInstitutionParameter.getUser();
		Invoice invoice = Invoice.find("id = " + id + " and institutionId = " + institution.getId()).first();
		render(invoice, user);
	}
}
