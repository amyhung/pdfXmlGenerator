package mpos.pdfXmlGenerator;

import java.util.Comparator;

public class SortByOrder implements Comparator<XmlItem> {

	public int compare(XmlItem a, XmlItem b) {
		return a.getOrder() - b.getOrder();
	}

}
