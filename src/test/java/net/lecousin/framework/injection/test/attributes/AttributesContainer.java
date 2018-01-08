package net.lecousin.framework.injection.test.attributes;

import java.util.ArrayList;

import net.lecousin.framework.math.IntegerUnit.Unit;
import net.lecousin.framework.math.TimeUnit;

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
	
	@Unit(TimeUnit.Day.class)
	public byte days;
	
	@Unit(TimeUnit.Hour.class)
	public short hours;
	
	@Unit(TimeUnit.Minute.class)
	public int minutes;
	
	@Unit(TimeUnit.Second.class)
	public long seconds;
	
}
