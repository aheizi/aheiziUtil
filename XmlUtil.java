package com.liantuo.weixin.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;

public class XmlUtil {

	//存放对类创建JAXBContext对象
	private static ConcurrentHashMap<String, JAXBContext> jaxbcontextmap = new ConcurrentHashMap<String, JAXBContext>();
	
	/**
	 * 创建单例JAXBContext对象，并将对象放到ConcurrentHashMap中
	 * @param c Class
	 * @return
	 * @throws JAXBException
	 */
	public static JAXBContext getJAXBContext(Class<?> c) throws JAXBException {
		JAXBContext context = jaxbcontextmap.get(c.getName());
		if(context != null){
			return context;
		} else {
			context = JAXBContext.newInstance(c);
			jaxbcontextmap.put(c.getName(), context);
			return context;
		}
    }
	
    /**
     * 把xml配置转换成对象
     * @param xml
     * @param classObj
     * @return
     */
    public static Object unmarshal(String xml, Class<?> classObj) {
        Object obj;
        try {
            JAXBContext jaxbContext = getJAXBContext(classObj);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            obj = unmarshaller.unmarshal(new StringReader(xml));
            return obj;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把对象转换成xml配置
     * @param classObj
     * @param obj
     * @return
     */
    public static String marshal(Class<?> classObj, Object obj,boolean fragment) {
        String xmlStr = "";
        try {
            JAXBContext jaxbContext = getJAXBContext(classObj);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);//是否省略xml头信息
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串
            StringWriter out = new StringWriter();
            marshaller.marshal(obj, new StreamResult(out));
            xmlStr = out.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return xmlStr.toString();
    }
	
}
