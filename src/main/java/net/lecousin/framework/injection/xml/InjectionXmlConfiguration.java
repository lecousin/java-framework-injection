package net.lecousin.framework.injection.xml;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.lecousin.framework.application.LCCore;
import net.lecousin.framework.concurrent.Task;
import net.lecousin.framework.concurrent.synch.ISynchronizationPoint;
import net.lecousin.framework.concurrent.synch.SynchronizationPoint;
import net.lecousin.framework.exception.NoException;
import net.lecousin.framework.injection.InjectionContext;
import net.lecousin.framework.io.IO;
import net.lecousin.framework.io.buffering.PreBufferedReadable;
import net.lecousin.framework.io.provider.IOProvider;
import net.lecousin.framework.xml.XMLStreamEvents;
import net.lecousin.framework.xml.XMLStreamReader;

/** Utility class to configure an InjectionContext from an XML file. */
public final class InjectionXmlConfiguration {
	
	private InjectionXmlConfiguration() { /* no instance */ }
	
	public static final String NAMESPACE_URI_0_1 = "http://code.lecousin.net/java/net.lecousin.framework.injection/0.1";
	
	/** Configure the given InjectionContext with the XML file. */
	@SuppressWarnings("resource")
	public static ISynchronizationPoint<Exception> configure(InjectionContext ctx, String filename) {
		IOProvider.Readable provider = LCCore.getApplication().getClassLoader().getIOProvider(filename);
		if (provider == null)
			return new SynchronizationPoint<>(new FileNotFoundException(filename));
		try {
			return configure(ctx, provider.provideIOReadable(Task.PRIORITY_RATHER_IMPORTANT));
		} catch (IOException e) {
			return new SynchronizationPoint<>(e);
		}
	}
	
	/** Configure the given InjectionContext with the XML file. */
	@SuppressWarnings("resource")
	public static ISynchronizationPoint<Exception> configure(InjectionContext ctx, IO.Readable xml) {
		IO.Readable.Buffered bio;
		if (xml instanceof IO.Readable.Buffered)
			bio = (IO.Readable.Buffered)xml;
		else
			bio = new PreBufferedReadable(xml, 16384, Task.PRIORITY_RATHER_IMPORTANT, 16384, Task.PRIORITY_NORMAL, 4);
		SynchronizationPoint<Exception> result = new SynchronizationPoint<>();
		XMLStreamReader.start(bio, 15000).listenInline(
			(reader) -> {
				new Task.Cpu<Void, NoException>("Parsing Injection XML file", Task.PRIORITY_NORMAL) {
					@Override
					public Void run() {
						try {
							configure(ctx, reader);
							result.unblock();
						} catch (Exception e) {
							result.error(e);
						} finally {
							xml.closeAsync();
						}
						return null;
					}
				}.start();
			},
			(error) -> {
				xml.closeAsync();
				result.error(error);
			},
			(cancel) -> {
				xml.closeAsync();
				result.cancel(cancel);
			}
		);
		return result;
	}

	private static void configure(InjectionContext ctx, XMLStreamReader xml) throws Exception {
		while (!XMLStreamEvents.Event.Type.START_ELEMENT.equals(xml.event.type)) {
			try { xml.next(); }
			catch (EOFException e) {
				throw new Exception("Invalid XML: no root element");
			}
		}
		configureInjection(ctx, xml);
	}
	
	/** Configure the given context with the given XML which must be on an Injection element. */
	public static void configureInjection(InjectionContext ctx, XMLStreamReader xml) throws Exception {
		if (!xml.event.localName.equals("Injection"))
			throw new Exception("Invalid XML: root element must be Injection");
		if (xml.event.namespaceURI.equals(NAMESPACE_URI_0_1))
			new InjectionXmlParser01().configureInjection(ctx, xml);
		else
			throw new Exception("Unknown injection namespace: " + xml.event.namespaceURI);
	}

}
