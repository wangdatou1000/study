package datou.redis;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private Jedis jedis;
	private JedisPool jedisPool;
	public void openRedis(){
		jedisPool=new JedisPool(new JedisPoolConfig(),"127.0.0.1",6379);
		jedis=jedisPool.getResource();
	}
	public void openRedisV2(){
		
		jedis=new Jedis("127.0.0.1", 6379);
	}
	public void operationRedis(){
		String name=jedis.get("name");
		System.out.println(name);
		jedis.set("name", "jedis--set-value");
		name=jedis.get("name");
		System.out.println(name);
		
		Map<String,String> map=new HashMap<>();
		map.put("time", "now");
		map.put("age", "10");
		map.put("name", "datou");
		jedis.hmset("wangdatou", map);
		System.out.println(jedis.hgetAll("wangdatou"));
	}
	public static void main( String[] args )
    {
    	System.out.println( "Hello World!" );
    	App a=new App();
    	//a.openRedis();
    	a.openRedisV2();
    	a.operationRedis();
    }
}
