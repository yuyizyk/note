package cn.yuyizyk.provide.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import cn.yuyizyk.provide.filter.RServerFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * 
 *
 * @author yuyi
 */
@Slf4j
// @Component
// @ConditionalOnExpression("#{ erc.rpc.enable-server != false }")
public class ServerRegistered implements RSRegister, ApplicationRunner {
	private ConfigProperties properties;
	private List<RSRegister> pRegisters = new ArrayList<>();
	private SpringServiceFinder finder = new SpringServiceFinder();
	private RpcServer rpcServer;
	private List<String> servernames = new ArrayList<>();
	private List<RServerFilter> rServerFilters = new ArrayList<>();

	public void setConfigurableListableBeanFactory(ConfigurableListableBeanFactory f) {
		finder.setFindServerCallBack(servernames::add);
		finder.setFindFilterCallBack(rServerFilters::add);
		f.addBeanPostProcessor(finder);
	}

	public void setProperties(ConfigProperties properties2) {
		this.properties = properties2;
		this.rpcServer = new RpcServer(properties.getLocalhostPort(), finder::getObj);
		pRegisters.add(this.rpcServer);
	}

	public ServerRegistered() {
	}

	@Override
	public RSRegister deregister() {
		pRegisters.forEach(RSRegister::deregister);
		return this;
	}

	@Override
	public RSRegister register() {
		if (servernames.isEmpty()) {
			log.info("canel register RPCServer . THE  servernames size is 0.");
			return this;
		}
		log.info("register RPCServer begin... ");
		new Thread(rpcServer::register).start();
		rpcServer.getInvokerHandler().addFilters(rServerFilters);
		// servernames.forEach(servername -> pRegisters.add(new
		// ConsulRegister(servername, properties.LocalhostAddress(),
		// properties.getLocalhostPort(), properties.PivotHostname(),
		// properties.PivotPort()).register()));
		log.info("register RPCServer end. ");
		return this;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		this.register();
	}

}
