package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import com.revenat.jmemcached.server.domain.CommandHandler;
import com.revenat.jmemcached.server.domain.ServerStorage;

/**
 * Parent class for all {@link CommandHandler} implementations
 * 
 * @author Vitaly Dragun
 *
 */
abstract class AbstractCommandHandler implements CommandHandler {
	private final ServerStorage storage;
	private AbstractCommandHandler nextInChain;
	
	AbstractCommandHandler(ServerStorage storage) {
		this.storage = requireNonNull(storage, "Storage can not be null");
	}

	/**
	 * Adds reference to next in chain {@link CommandHandler}
	 */
	void add(AbstractCommandHandler next) {
		if (nextInChain == null) {
			nextInChain = next;
		} else {
			nextInChain.add(next);
		}
	}
	
	final ServerStorage getStorage() {
		return this.storage;
	}
	
	final CommandHandler getNextInChain() {
		return nextInChain;
	}

}
