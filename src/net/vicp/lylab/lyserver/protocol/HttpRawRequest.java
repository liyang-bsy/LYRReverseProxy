package net.vicp.lylab.lyserver.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.vicp.lylab.core.CloneableBaseObject;
import net.vicp.lylab.core.model.Pair;

public class HttpRawRequest extends CloneableBaseObject {
	public static final byte[] spliter = "\r\n".getBytes();
	public static final byte[] doubleSpliter = "\r\n\r\n".getBytes();
	
	private List<Pair<String, String>> head;
	private Map<String, String> headSet;
	private Map<String, String> originalKey;
	private List<Byte> data;

	public HttpRawRequest() {
		super();
		head = new ArrayList<Pair<String, String>>();
		headSet = new HashMap<String, String>();
		originalKey = new HashMap<String, String>();
		data = new ArrayList<>();
	}

	public void addHead(String key, String value) {
		if(originalKey.containsKey(key.toLowerCase())) {
			String oriKey = originalKey.get(key.toLowerCase());
			headSet.remove(oriKey);
			Iterator<Pair<String, String>> it = head.iterator();
			while (it.hasNext())
				if (it.next().getLeft().equals(oriKey))
					it.remove();
		}
		originalKey.put(key.toLowerCase(), key);
		headSet.put(key, value);
		for (Pair<String, String> pair : head) {
			if (pair.getLeft().equals(key)) {
				pair.setRight(value);
				return;
			}
		}
		head.add(new Pair<String, String>(key, value));
	}

	public String getHead(String key) {
		return headSet.get(originalKey.get(key.toLowerCase()));
	}

	public List<Pair<String, String>> getAllHead() {
		return head;
	}

	public List<Byte> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "HttpRequest [head=" + head + ", data=" + data + "]";
	}

}
