package controllers.howtodo;

import java.io.File;
import java.util.ArrayList;

import models.howtodo.FreePage;
import models.howtodo.Parameter;
import models.howtodo.SequenceMailQueue;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.vfs.VirtualFile;
import util.Utils;

public class FreePageController extends Controller {

	@Before
	@Finally
	static void CORS() {
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9001/");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
		Http.Header hd = new Http.Header();
		hd.name = "Access-Control-Allow-Origin";
		hd.values = new ArrayList<String>();
		hd.values.add("http://localhost:9001");
		Http.Response.current().headers.put("Access-Control-Allow-Origin", hd);
	}

	public static void details(String friendlyUrl, long mid) {
		/* Verifying click on mail link */
		if (!Utils.isNullOrZero(mid)) {
			SequenceMailQueue seqMail = SequenceMailQueue.findById(mid);
			if (seqMail != null) {
				seqMail.setMailRead(true);
				seqMail.setMailClicked(true);
				seqMail.save();
			}
		}
		FreePage freePage = FreePage.findByFriendlyUrl(friendlyUrl);
		Parameter parameter = Parameter.all().first();
		String title = Utils.removeHTML(freePage.getMainTitle());
		String headline = Utils.removeHTML(freePage.getSubtitle1());
		/* Verify if test A/B of Video or Text is enabled. */
		/*
		 * If yes, record a cookie in user navigator to guarantee that he will
		 * see only one page.
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
		render(freePage, parameter, title, headline);
	}

	public static void getBackgroundImage(long id) {
		final FreePage freePage = FreePage.findById(id);
		notFoundIfNull(freePage);
		if (freePage.getBackgroundImage() != null) {
			renderBinary(freePage.getBackgroundImage().get());
			return;
		}
	}

	public static File getVirtualFile() {
		VirtualFile vf = VirtualFile.fromRelativePath("/public/images/binarybg.jpg");
		File f = vf.getRealFile();
		return f;
	}

	public static void main(String[] args) {
		System.out.println(Utils.isNullOrZero(1l));
	}
	
	public static void getImage(long id, String index) {
		final FreePage freePage = FreePage.findById(id);
		notFoundIfNull(freePage);
		if ("1".equals(index)) {
			if (freePage.getImage1() != null) {
				renderBinary(freePage.getImage1().get(), index.concat("-").concat(freePage.friendlyUrl));
				return;
			}
		} else if ("2".equals(index)) {
			if (freePage.getImage2() != null) {
				renderBinary(freePage.getImage2().get(), index.concat("-").concat(freePage.friendlyUrl));
				return;
			}
		} else if ("3".equals(index)) {
			if (freePage.getImage3() != null) {
				renderBinary(freePage.getImage3().get(), index.concat("-").concat(freePage.friendlyUrl));
				return;
			}
		} else if ("4".equals(index)) {
			if (freePage.getImage4() != null) {
				renderBinary(freePage.getImage4().get(), index.concat("-").concat(freePage.friendlyUrl));
				return;
			}
		} else if ("5".equals(index)) {
			if (freePage.getImage5() != null) {
				renderBinary(freePage.getImage5().get(), index.concat("-").concat(freePage.friendlyUrl));
				return;
			}
		} else {
			getVirtualFile();
		}
	}
}
