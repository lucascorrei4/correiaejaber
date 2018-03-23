package controllers.howtodo;

import java.util.List;

import controllers.CRUD;
import controllers.Security;
import models.howtodo.LeadSearchAnswer;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.Before;
import util.PlansEnum;

@CRUD.For(models.howtodo.LeadSearchAnswer.class)
public class LeadSearchAnswerCRUD extends CRUD {
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
		renderArgs.put("planSPO02", PlansEnum.isPlanSPO02(AdminPub.getInstitutionInvoice().getPlan().getValue()));
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
			render("LeadSearchAnswerCRUD/list.html", type, objects, count, totalCount, page, orderBy, order);
		}
	}

	public static void remove(String id) throws Exception {
		LeadSearchAnswer leadSearchAnswer = LeadSearchAnswer.find("id = " + Long.valueOf(id)).first();
		leadSearchAnswer.delete();
		LeadSearchAnswerCRUD.list(0, null, null, null, null);
	}
}
