package org.openmrs.module.ugandaemrpoc.utils;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {

	public static Date dateFormtterDate(Date date, String time) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");

		String formattedDate = formatterExt.format(date) + " " + time;

		return formatter.parse(formattedDate);
	}

	public static String dateFormtterString(Date date, String time) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		SimpleDateFormat formatterExt = new SimpleDateFormat("yyyy-MM-dd");

		String formattedDate = formatterExt.format(date) + " " + time;

		return formattedDate;
	}

	public static Date formatDateToCompatibleDate(Date date, String time) throws ParseException {
		AdministrationService administrationService = Context.getAdministrationService();
		SimpleDateFormat formatter = new SimpleDateFormat(
				administrationService.getGlobalProperty("ugandaemrpoc.displayDateFormat"));

		SimpleDateFormat formatterExt = new SimpleDateFormat(
				administrationService.getGlobalProperty("ugandaemrpoc.defaultDateFormat"));

		String formattedDate = formatterExt.format(date) + " " + time;

		return formatter.parse(formattedDate);
	}

	public static String formatDateToCompatibleDateString(Date date, String time) {

		SimpleDateFormat formatterExt = new SimpleDateFormat(Context.getAdministrationService().getGlobalProperty(
				"ugandaemrpoc.defaultDateFormat"));

		String formattedDate = "";
		formattedDate = formatterExt.format(date) + " " + time;

		return formattedDate;
	}
}
