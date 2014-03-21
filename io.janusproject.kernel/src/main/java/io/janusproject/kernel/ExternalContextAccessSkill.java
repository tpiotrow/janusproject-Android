/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.kernel;

import io.janusproject.repository.ContextRepository;
import io.sarl.core.Behaviors;
import io.sarl.core.ContextJoined;
import io.sarl.core.ContextLeft;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.MemberJoined;
import io.sarl.core.MemberLeft;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Skill;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/** Skill that permits to access to the context in which the agent is located.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class ExternalContextAccessSkill extends Skill implements ExternalContextAccess{

	private Set<UUID> contexts = Sets.newConcurrentHashSet();
	
	private final ContextRepository contextRepository;
	
	/**
	 * @param agent - owner of the skill.
	 * @param contextRepository - repository of the contexts.
	 */
	public ExternalContextAccessSkill(Agent agent, ContextRepository contextRepository) {
		super(agent);
		this.contextRepository = contextRepository;
	}
	
	@Override
	protected void install() {
		super.install();
		AgentContext ac = this.contextRepository.getContext(getOwner().getParentID());
		this.join(ac.getID(), ac.getDefaultSpace().getID().getID());	
	}
	
	@Override
	protected void uninstall() {
		//Leave all contexts including the default one.
		for (UUID contextID : this.contexts) {
			this.leave(contextID);
		}
		super.uninstall();
	}

	@Override
	public Collection<AgentContext> getAllContexts() {
		return this.contextRepository.getContexts(this.contexts);
	}

	@Override
	public AgentContext getContext(UUID contextID) {
		Preconditions.checkNotNull(contextID);
		if(!this.contexts.contains(contextID)){
			throw new IllegalArgumentException(Locale.getString("UNKNOWN_CONTEXT_ID", contextID)); //$NON-NLS-1$
		}
		return this.contextRepository.getContext(contextID);
	}

	@Override
	public void join(UUID futureContext, UUID futureContextDefaultSpaceID) {
		Preconditions.checkNotNull(futureContext);
		Preconditions.checkNotNull(futureContextDefaultSpaceID);
		
		AgentContext ac = this.contextRepository.getContext(futureContext);
		
		Preconditions.checkNotNull(ac, "Unknown Context"); //$NON-NLS-1$
		
		if(this.contexts.contains(futureContext)){			
			return;
		}
		
		
		if(ac.getDefaultSpace().getID().getID() != futureContextDefaultSpaceID){
			throw new IllegalArgumentException(Locale.getString("INVALID_DEFAULT_SPACE_MATCHING", futureContextDefaultSpaceID)); //$NON-NLS-1$
		}
		
		this.contexts.add(futureContext);
		BehaviorsAndInnerContextSkill imp = (BehaviorsAndInnerContextSkill) getSkill(Behaviors.class);
		imp.registerOnDefaultSpace((EventSpaceImpl) ac.getDefaultSpace());
		
		fireContextJoined(futureContext,futureContextDefaultSpaceID);
		fireMemberJoined(ac);
	}
	
	/**
	 * Fires an {@link ContextJoined} event into the Inner Context default space of the owner agent to 
	 * notify behaviors/members that a new context has been joined.
	 * @param futureContext - ID of the newly joined context
	 * @param futureContextDefaultSpaceID - ID of the default space of the newly joined context
	 */
	protected void fireContextJoined(UUID futureContext, UUID futureContextDefaultSpaceID) {
		ContextJoined event = new ContextJoined();
		event.setDefaultSpaceID(futureContextDefaultSpaceID);
		event.setHolonContextID(futureContext);
		getSkill(Behaviors.class).wake(event);
	}
	
	/**
	 * Fires an {@link MemberJoined} event into the newly joined parent Context default space to 
	 * notify other context's members that a new agent joined this context.
	 * @param newJoinedContext - the newly joined context to notify its members
	 */
	protected void fireMemberJoined(AgentContext newJoinedContext) {
		EventSpace defSpace = newJoinedContext.getDefaultSpace();
		MemberJoined event = new MemberJoined();
		event.setAgentID(this.getOwner().getID());
		event.setParentContextID(newJoinedContext.getID());
		event.setSource(defSpace.getAddress(getOwner().getID()));
		defSpace.emit(event);
	}

	@Override
	public void leave(UUID contextID) {
		Preconditions.checkNotNull(contextID);
		AgentContext ac = this.contextRepository.getContext(contextID);
		Preconditions.checkNotNull(ac, "Unknown Context"); //$NON-NLS-1$
		if(!this.contexts.contains(contextID)){
			return;
		}
		//TO send this event the agent must still be inside the context and its default space
		fireContextLeft(contextID);
		fireMemberLeft(ac);
		
		BehaviorsAndInnerContextSkill imp = (BehaviorsAndInnerContextSkill) getSkill(Behaviors.class);
		imp.unregisterFromDefaultSpace((EventSpaceImpl) ac.getDefaultSpace());
		this.contexts.remove(contextID);		
	}
	
	/**
	 * Fires an {@link ContextLeft} event into the Inner Context Default space of the owner agent to 
	 * notify behaviors/members that the specified context has been left.
	 * @param contextID - the ID of context that will be left
	 */
	protected void fireContextLeft(UUID contextID) {
		ContextLeft event = new ContextLeft();		
		event.setHolonContextID(contextID);
		getSkill(Behaviors.class).wake(event);
	}
		
	/**
	 * Fires an {@link MemberLeft} event into the default space of the Context that will be left to 
	 * notify other context's members that an agent has left this context.
	 * @param leftContext - the context that will be left
	 */
	protected void fireMemberLeft(AgentContext leftContext) {
		EventSpace defSpace = leftContext.getDefaultSpace();
		MemberLeft event = new MemberLeft();
		event.setAgentID(this.getOwner().getID());		
		event.setSource(defSpace.getAddress(getOwner().getID()));
		defSpace.emit(event);
	}

	
}
