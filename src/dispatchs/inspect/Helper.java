package dispatchs.inspect;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import fj.Ord;
import fj.P;
import fj.P2;
import fj.Show;
import fj.data.List;
import fj.data.Option;
import fj.data.Set;
import fj.data.TreeMap;

public class Helper {

	// some global state 
	public static TreeMap<String, TreeMap<String, Method>> methodMap = TreeMap.empty(Ord.stringOrd);
	public static TreeMap<String, List<List<Class>>>       multipleDispatch = TreeMap.empty(Ord.stringOrd); 
	
	// the main resolver interface
	static public interface Resolver {
		String choose(Object... args);
	}
	
	// a neat solution for allowing a declarative style resolver function
	static public abstract class Pred{
		public abstract boolean is(Object o);
	}
	
	// match on object's class
	static public class Obj extends Pred {
		Class cls;
		public Obj(Class cls) {	this.cls = cls;	}
		@Override public boolean is(Object o) {	return cls.isAssignableFrom(o.getClass());}
	}
	
	// match on object's value
	static public class Val extends Pred {
		Object val;
		public Val(Object val){	this.val = val;	}
		@Override public boolean is(Object o) {return val.getClass().isAssignableFrom(o.getClass()) && val.equals(o);}
	}
	
	// match on both class and value
	static public class ObjVal extends Pred {
		Class cls;
		Object val;
		public ObjVal(Class cls, Object val) { this.val = val; this.cls = cls; }
		@Override public boolean is(Object o) {return cls.isAssignableFrom(o.getClass()) && val.equals(o);}
	}
	
	// a comfortable builder for the declarative resolve solution
	static public class Builder {
		List<P2<List<Pred>, String>> l = List.nil();
		public Builder obj   (List<Class>  cls, String s) {l = l.cons(P.p(cls.map(x->new Obj(x)), s)); return this;}
		public Builder val   (List<Object> obs, String s) {l = l.cons(P.p(obs.map(x->new Val(x)), s)); return this;}
		public Builder objVal(List<Class> cls, List<Object> obs, String s) 
  			{l = l.cons(P.p(cls.zip(obs).map(p->new ObjVal(p._1(), p._2())), s)); return this;}
		public List<P2<List<Pred>, String>> build() {
			return l.reverse();
		}
	}
	
	// this function finds the first matching method based on Pred.is method hierarchy 
	static public String resolveList(List<P2<List<Pred>, String>> b, List<Object> args){
		return b.find(x->x._1().zip(args).forall(y->y._1().is(y._2()))).map(P2.__2()).orSome("Error");
	}
	
	static public String resolveList(List<P2<List<Pred>, String>> b, Object... args){
		return resolveList(b, List.list(args));
	}
		
}
