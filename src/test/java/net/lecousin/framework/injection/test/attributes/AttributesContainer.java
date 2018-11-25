package net.lecousin.framework.injection.test.attributes;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.lecousin.framework.locale.annotations.LocalizableProperty;
import net.lecousin.framework.math.FragmentedRangeInteger;
import net.lecousin.framework.math.IntegerUnit.Unit;
import net.lecousin.framework.math.RangeInteger;
import net.lecousin.framework.math.TimeUnit;
import net.lecousin.framework.util.StringFormat;
import net.lecousin.framework.util.StringParser;

public class AttributesContainer implements AttributesInterface {

	public boolean bool1 = false;
	public Boolean bool2 = null;
	
	public byte b1 = 0;
	public Byte b2 = null;
	public short s1 = 0;
	public Short s2 = null;
	public int i1 = 0;
	public Integer i2 = null;
	public long l1 = 0;
	public Long l2 = null;
	
	public String strNull = "notnull";
	public String str = null;
	
	public ArrayList<Integer> integers;
	public ArrayList<String> strings;
	public Integer[] integers2;
	public Integer[] integers3;
	
	@LocalizableProperty(key="test", name = "test")
	@Unit(TimeUnit.Day.class)
	public byte days;
	
	@Unit(TimeUnit.Hour.class)
	@LocalizableProperty(key="test", name = "test")
	public short hours;
	
	@LocalizableProperty(key="test", name = "test")
	@Unit(TimeUnit.Minute.class)
	public int minutes;
	
	@Unit(TimeUnit.Second.class)
	@LocalizableProperty(key="test", name = "test")
	public long seconds;
	
	public FragmentedRangeInteger ranges;
	public RangeInteger range;
	
	@StringFormat(parser=CustomParser.class)
	public Integer custom;
	@StringFormat(parser=CustomParser.class)
	public Integer custom2;
	
	public static class CustomParser implements StringParser<Integer> {
		@Override
		public Integer parse(String string) throws ParseException {
			return "hello".equals(string) ? Integer.valueOf(51) : Integer.valueOf(0);
		}
	}
	
	public List<String> strList;
	public Map<String, String> map;
	
	public void destroy() {
	}
	
}
