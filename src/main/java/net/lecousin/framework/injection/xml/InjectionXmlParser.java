package net.lecousin.framework.injection.xml;

import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.xml.XMLStreamReader;

/** Interface to parse XML stream and configure an InjectionContext. */
public interface InjectionXmlParser {

	/** Entry-point of the parser. */
	void configureInjection(InjectionContext ctx, XMLStreamReader xml) throws Exception;
	
}
