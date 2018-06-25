package sample.controller;

import com.lacunasoftware.pkiexpress.Authentication;
import com.lacunasoftware.pkiexpress.InstallationNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.util.Util;

import java.io.IOException;

@Controller
public class CheckInstallationController {

    @RequestMapping(value = "/check-installation", method = {RequestMethod.GET})
    public String get(
            @RequestParam(value = "controller") String controller,
            @RequestParam(value = "goto", required = false) String goTo
    ) throws IOException {

        try {
            Authentication auth = new Authentication();
            Util.setPkiDefaults(auth);
            auth.start();
        } catch (InstallationNotFoundException ex) {
            return "installation-not-found";
        }

        if (goTo != null && goTo.length() > 0) {
            return "redirect:/" + controller + "?goto=" + goTo;
        }
        return "redirect:/" + controller;
    }
}
