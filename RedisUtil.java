package com.liantuo.weixin.util.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.liantuo.weixin.util.file.FileUtil;

/**
 * Redis工具类
 * @author tengbx
 * 
 */
public class RedisUtil {
	private static String ip = "192.168.20.97";
	private static int port = 6379;
	private static int maxActive = 200;
	private static int maxIdle = 20;
	private static int maxWait = 1000;
	private static int dataBase = 6;
	private static int timeOut = 3000;
	private static int expire= 1800;
	private static JedisPool pool = null;
	static {
		Properties property = FileUtil.loadProperties(
				"config/properties/servers.properties", 1000);
		if (property != null) {
			ip = property.getProperty("redis.ip");
			port = Integer.parseInt(property.getProperty("redis.port"));
			maxActive = Integer.parseInt(property
					.getProperty("redis.maxactive"));
			maxIdle = Integer.parseInt(property.getProperty("redis.maxidle"));
			maxWait = Integer.parseInt(property.getProperty("redis.maxwait"));
			dataBase = Integer.parseInt(property.getProperty("redis.database"));
			timeOut = Integer.parseInt(property.getProperty("redis.timeout"));
			expire = Integer.parseInt(property.getProperty("redis.expire"));
		}
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxActive(maxActive);
		config.setMaxIdle(maxIdle);
		config.setMaxWait(maxWait);
		config.setTestOnBorrow(true);
		pool = new JedisPool(config, ip, port, timeOut, null, dataBase);
	}

	/**
	 * 判断键是否存在
	 * 
	 * @param key
	 * @return
	 */
	public static boolean ifExist(String key) {
		Jedis jedis = pool.getResource();
		boolean result = jedis.exists(key.getBytes());
		pool.returnResource(jedis);
		return result;
	}

	/**
	 * 获取键对应的值
	 * 
	 * @param key
	 * @return
	 */
	private static byte[]  getValue(String key) {
		Jedis jedis = pool.getResource();
		byte[] result = jedis.get(key.getBytes());
		pool.returnResource(jedis);
		return result;
	}

	/**
	 * 存储键值对
	 * 
	 * @param key
	 * @return
	 */
	private static void setValue(String key, byte[] value) {
		Jedis jedis = pool.getResource();
		jedis.set(key.getBytes(), value);
		jedis.expire(key.getBytes(), expire);//设置失效期
		pool.returnResource(jedis);
	}
	
	/**
	 * 删除键值
	 * 
	 * @param key
	 * @return
	 */
	public static void removekey(String key) {
		Jedis jedis = pool.getResource();
		jedis.expire(key.getBytes(), -1);//设置失效期立即失效
		pool.returnResource(jedis);
	}
	
	/**
	 * 存储键值对  值为object  
	 * 
	 * @param key
	 * @param map
	 * @return
	 */
	public static void setObjectValue(String key, Object object) {
		setValue(key, RedisSerializeUtil.serialize(object));
	}
	
	/**
	 * 存储键值对  值为object  
	 * @param key  redis 的 key
	 * @param mapkey  redis Mapvalue  的key
	 * @param object  redis Mapvalue  的value
	 * @return
	 */
	public static void setMapValue(String key,String mapkey ,Object object) {
		 Map<String, Object> map = getMapValue(key);
		 if(map==null){
			 map= new HashMap<String, Object>();
		 }
		 map.put(mapkey, object);
		 setObjectValue(key,map);
	}
	
	/**
	 * 如果存在key，存储键值对，值为object  
	 * @param key  redis 的 key
	 * @param mapkey  redis Mapvalue  的key
	 * @param object  redis Mapvalue  的value
	 * @return
	 */
	public static void setMapValueifExist(String key,String mapKey ,Object object) {
		if(ifExist(key)){
			if(getMapValue(mapKey)!=null&&getMapValue(mapKey).containsKey(mapKey)){
				setMapValue(key, mapKey, object);
			}
		}
	}
	
	/**
	 * 获取键对应的值  值为Oject
	 * 
	 * @param key
	 * @return
	 */
	public static Object getObjectValue(String key) {
		return RedisSerializeUtil.unserialize(getValue(key));
	}
	
	/**
	 * 获取键对应的值  值为map
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getMapValue(String key) {
		return (Map<String,Object>)getObjectValue(key);
	}
	
	/**
	 * 获取键对应的值  值为map 
	 * @param key
	 * @param mapKey
	 * @return  返回mapKey对应的值
	 */
	public static Object getMapValue(String key,String mapKey) {
		Map<String, Object> map = getMapValue(key);
		if(map==null){
			return null;
		}
		return map.get(mapKey);
	}
}
