package org.rapidoid.inject.app;

/*
 * #%L
 * rapidoid-ioc
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.inject.IoC;
import org.rapidoid.test.TestCommons;
import org.testng.annotations.Test;

@Authors("Nikolche Mihajlovski")
@Since("2.0.0")
public class AppInjectionTest extends TestCommons {

	@Test
	public void shouldInjectAndCallPostConstruct() {
		IoC.manage(App.class, PersonServiceImpl.class);
		isTrue(App.READY);

		App app = IoC.singleton(App.class);
		same(app, IoC.singleton(App.class), IoC.singleton(App.class));

		notNull(app.personService);
		notNull(app.bookService);
		notNull(app.bookService.dao);

		same(app.personService, app.personService2);

		same(app.logger, app.personService2.logger, app.bookService.logger, app.bookService.dao.logger);
	}

}
