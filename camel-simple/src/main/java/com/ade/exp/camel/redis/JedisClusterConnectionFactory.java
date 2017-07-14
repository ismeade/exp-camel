package com.ade.exp.camel.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.ClusterStateFailureException;
import org.springframework.data.redis.ExceptionTranslationStrategy;
import org.springframework.data.redis.PassThroughExceptionTranslationStrategy;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.connection.jedis.JedisClusterConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.*;

import java.util.*;


/**
 *
 * Created by liyang on 2017/7/14.
 */
public class JedisClusterConnectionFactory implements InitializingBean, DisposableBean, RedisConnectionFactory {

	private final static Log log = LogFactory.getLog(JedisConnectionFactory.class);
	private static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION = new PassThroughExceptionTranslationStrategy(
			JedisConverters.exceptionConverter());

	private String hostName = "localhost";
	private int port = Protocol.DEFAULT_PORT;
	private int timeout = Protocol.DEFAULT_TIMEOUT;
	private String password;
	private JedisPoolConfig poolConfig = new JedisPoolConfig();
	private JedisCluster cluster;
	private ClusterCommandExecutor clusterCommandExecutor;

	public JedisClusterConnectionFactory(JedisPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	public void afterPropertiesSet() {
		this.cluster = createCluster();
	}

	private JedisCluster createCluster() {
		JedisCluster cluster = createCluster(this.poolConfig);
		this.clusterCommandExecutor = new ClusterCommandExecutor(new JedisClusterTopologyProvider(
				cluster), new JedisClusterNodeResourceProvider(cluster), EXCEPTION_TRANSLATION);
		return cluster;
	}

	protected JedisCluster createCluster(GenericObjectPoolConfig poolConfig) {
		return new JedisCluster(new HostAndPort(hostName, port), 5000, 5000, 10, password, poolConfig);
	}

	public void destroy() {
		if (cluster != null) {
			try {
				cluster.close();
			} catch (Exception ex) {
				log.warn("Cannot properly close Jedis cluster", ex);
			}
			try {
				clusterCommandExecutor.destroy();
			} catch (Exception ex) {
				log.warn("Cannot properly close cluster command executor", ex);
			}
		}
	}

	public RedisConnection getConnection() {
		return getClusterConnection();
	}

	@Override
	public RedisClusterConnection getClusterConnection() {
		if (cluster == null) {
			throw new InvalidDataAccessApiUsageException("Cluster is not configured!");
		}
		return new JedisClusterConnection(cluster, clusterCommandExecutor);
	}

	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return EXCEPTION_TRANSLATION.translate(ex);
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public JedisPoolConfig getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(JedisPoolConfig poolConfig) {
		this.poolConfig = poolConfig;
	}

	public boolean getConvertPipelineAndTxResults() {
		return true;
	}

	@Override
	public RedisSentinelConnection getSentinelConnection() {
        return null;
	}

    static class JedisClusterNodeResourceProvider implements ClusterNodeResourceProvider {

		private final JedisCluster cluster;

		/**
		 * Creates new {@link JedisClusterNodeResourceProvider}.
		 *
		 * @param cluster must not be {@literal null}.
		 */
		public JedisClusterNodeResourceProvider(JedisCluster cluster) {
			this.cluster = cluster;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.redis.connection.ClusterNodeResourceProvider#getResourceForSpecificNode(org.springframework.data.redis.connection.RedisClusterNode)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Jedis getResourceForSpecificNode(RedisClusterNode node) {

			JedisPool pool = getResourcePoolForSpecificNode(node);
			if (pool != null) {
				return pool.getResource();
			}

			throw new IllegalArgumentException(String.format("Node %s is unknown to cluster", node));
		}

		protected JedisPool getResourcePoolForSpecificNode(RedisNode node) {

			Assert.notNull(node, "Cannot get Pool for 'null' node!");

			Map<String, JedisPool> clusterNodes = cluster.getClusterNodes();
			if (clusterNodes.containsKey(node.asString())) {
				return clusterNodes.get(node.asString());
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.redis.connection.ClusterNodeResourceProvider#returnResourceForSpecificNode(org.springframework.data.redis.connection.RedisClusterNode, java.lang.Object)
		 */
		@Override
		public void returnResourceForSpecificNode(RedisClusterNode node, Object client) {
			getResourcePoolForSpecificNode(node).returnResource((Jedis) client);
		}

	}

	/**
	 * Jedis specific implementation of {@link ClusterTopologyProvider}.
	 *
	 * @author Christoph Strobl
	 * @since 1.7
	 */
	static class JedisClusterTopologyProvider implements ClusterTopologyProvider {

		private final Object lock = new Object();
		private final JedisCluster cluster;
		private long time = 0;
		private ClusterTopology cached;

		/**
		 * Create new {@link JedisClusterTopologyProvider}.s
		 *
		 * @param cluster
		 */
		public JedisClusterTopologyProvider(JedisCluster cluster) {
			this.cluster = cluster;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.redis.connection.ClusterTopologyProvider#getTopology()
		 */
		@Override
		public ClusterTopology getTopology() {

			if (cached != null && time + 100 > System.currentTimeMillis()) {
				return cached;
			}

			Map<String, Exception> errors = new LinkedHashMap<String, Exception>();

			for (Map.Entry<String, JedisPool> entry : cluster.getClusterNodes().entrySet()) {

				Jedis jedis = null;

				try {
					jedis = entry.getValue().getResource();

					time = System.currentTimeMillis();
					Set<RedisClusterNode> nodes = Converters.toSetOfRedisClusterNodes(jedis.clusterNodes());

					synchronized (lock) {
						cached = new ClusterTopology(nodes);
					}
					return cached;
				} catch (Exception ex) {
					errors.put(entry.getKey(), ex);
				} finally {
					if (jedis != null) {
						entry.getValue().returnResource(jedis);
					}
				}
			}

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Exception> entry : errors.entrySet()) {
				sb.append(String.format("\r\n\t- %s failed: %s", entry.getKey(), entry.getValue().getMessage()));
			}
			throw new ClusterStateFailureException(
					"Could not retrieve cluster information. CLUSTER NODES returned with error." + sb.toString());
		}
	}

}