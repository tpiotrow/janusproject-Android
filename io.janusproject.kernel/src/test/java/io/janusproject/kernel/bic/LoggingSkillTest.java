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
package io.janusproject.kernel.bic;

import io.janusproject.services.logging.LogService;
import io.janusproject.util.LoggerCreator;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Capacity;

import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class LoggingSkillTest extends Assert {

	@Mock
	protected Handler handler;
	
	protected Agent owner;
	
	@Mock
	protected LogService logService;

	@Mock
	protected Logger parentLogger;

	@InjectMocks
	protected LoggingSkill skill;
	
	protected Logger logger;
	
	@Before
	public void setUp() throws Exception {
		UUID agentId = UUID.randomUUID();
		this.owner = new Agent(agentId) {
			@Override
			protected <S extends Capacity> S getSkill(Class<S> capacity) {
				return capacity.cast(LoggingSkillTest.this.skill);
			}
		};
		this.owner = Mockito.spy(this.owner);
		this.skill = new LoggingSkill(this.owner);
		MockitoAnnotations.initMocks(this);
		this.skill = Mockito.spy(this.skill);
		//
		Mockito.when(this.logService.getLogger()).thenReturn(this.parentLogger);
		//
		Mockito.when(this.parentLogger.getHandlers()).thenReturn(new Handler[] {this.handler});
		//
		this.skill.install();
		//
		this.logger = Mockito.spy(this.skill.getLogger());
		this.skill.setLogger(this.logger);
	}

	@After
	public void tearDown() throws Exception {
		this.skill = null;
		this.logger = null;
		this.owner = null;
		this.handler = null;
		this.logService = null;
		this.parentLogger = null;
		
	}

	@Test
	public void errorObject_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.error(message);
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		Mockito.verify(this.parentLogger, Mockito.times(1)).getLevel();
		Mockito.verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void errorObject_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.error(message);
		//
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		//
		Mockito.verify(this.logger, new Times(1)).log(argument2.capture(), argument3.capture());
		assertSame(Level.SEVERE, argument2.getValue());
		assertEquals(message, argument3.getValue());
		//
		Mockito.verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.SEVERE, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void errorObjectThrowable_off() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.error(message, ex);
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		Mockito.verify(this.parentLogger, Mockito.times(1)).getLevel();
		Mockito.verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void errorObjectThrowable_on() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		ArgumentCaptor<Throwable> argument5 = ArgumentCaptor.forClass(Throwable.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.error(message, ex);
		//
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		//
		Mockito.verify(this.logger, new Times(1)).log(argument2.capture(), argument3.capture(), argument5.capture());
		assertSame(Level.SEVERE, argument2.getValue());
		assertEquals(message, argument3.getValue());
		assertSame(ex, argument5.getValue());
		//
		Mockito.verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.SEVERE, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertSame(ex, argument4.getValue().getThrown());
	}

	@Test
	public void warningObject_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.warning(message);
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		Mockito.verify(this.parentLogger, Mockito.times(1)).getLevel();
		Mockito.verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void warningObject_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.warning(message);
		//
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		//
		Mockito.verify(this.logger, new Times(1)).log(argument2.capture(), argument3.capture());
		assertSame(Level.WARNING, argument2.getValue());
		assertEquals(message, argument3.getValue());
		//
		Mockito.verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.WARNING, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void warningObjectThrowable_off() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.warning(message, ex);
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		Mockito.verify(this.parentLogger, Mockito.times(1)).getLevel();
		Mockito.verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void warningObjectThrowable_on() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		ArgumentCaptor<Throwable> argument5 = ArgumentCaptor.forClass(Throwable.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.warning(message, ex);
		//
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		//
		Mockito.verify(this.logger, new Times(1)).log(argument2.capture(), argument3.capture(), argument5.capture());
		assertSame(Level.WARNING, argument2.getValue());
		assertEquals(message, argument3.getValue());
		assertSame(ex, argument5.getValue());
		//
		Mockito.verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.WARNING, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertSame(ex, argument4.getValue().getThrown());
	}

	@Test
	public void info_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.info(message);
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.INFO, argument1.getValue());
		Mockito.verify(this.parentLogger, Mockito.times(1)).getLevel();
		Mockito.verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void info_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.info(message);
		//
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.INFO, argument1.getValue());
		//
		Mockito.verify(this.logger, new Times(1)).log(argument2.capture(), argument3.capture());
		assertSame(Level.INFO, argument2.getValue());
		assertEquals(message, argument3.getValue());
		//
		Mockito.verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.INFO, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void println_off() {
		info_off();
	}

	@Test
	public void println_on() {
		info_on();
	}

	@Test
	public void debug_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.debug(message);
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.CONFIG, argument1.getValue());
		Mockito.verify(this.parentLogger, Mockito.times(1)).getLevel();
		Mockito.verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void debug_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.debug(message);
		//
		Mockito.verify(this.logger, new Times(1)).isLoggable(argument1.capture());
		assertSame(Level.CONFIG, argument1.getValue());
		//
		Mockito.verify(this.logger, new Times(1)).log(argument2.capture(), argument3.capture());
		assertSame(Level.CONFIG, argument2.getValue());
		assertEquals(message, argument3.getValue());
		//
		Mockito.verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.CONFIG, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void getLogLevel() {
		int expected = LoggerCreator.toInt(this.logger.getLevel());
		assertEquals(expected, this.skill.getLogLevel());
	}

	@Test
	public void setLogLevel() {
		for(int i = 0; i < 10; i++) {
			this.skill.setLogLevel(i);
			assertEquals(Math.max(0, Math.min(7, i)), this.skill.getLogLevel());
		}
	}

	@Test
	public void isErrorLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(1);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(2);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(3);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isErrorLogEnabled());
	}

	@Test
	public void isWarningLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(1);
		assertFalse(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(2);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(3);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isWarningLogEnabled());
	}

	@Test
	public void isInfoLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(1);
		assertFalse(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(2);
		assertFalse(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(3);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isInfoLogEnabled());
	}

	@Test
	public void isDebugLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(1);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(2);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(3);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isDebugLogEnabled());
	}

}
