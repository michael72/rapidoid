package org.rapidoid.http.fast.handler;

/*
 * #%L
 * rapidoid-http-fast
 * %%
 * Copyright (C) 2014 - 2015 Nikolche Mihajlovski and contributors
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

import java.util.concurrent.Callable;

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.commons.MediaType;
import org.rapidoid.http.Req;
import org.rapidoid.http.fast.FastHttp;
import org.rapidoid.http.fast.HttpWrapper;
import org.rapidoid.net.abstracts.Channel;

@Authors("Nikolche Mihajlovski")
@Since("4.3.0")
public class FastCallableHttpHandler extends AbstractAsyncHttpHandler {

	private final Callable<Object> handler;

	public FastCallableHttpHandler(FastHttp http, MediaType contentType, HttpWrapper[] wrappers,
			Callable<Object> handler) {
		super(http, contentType, wrappers);
		this.handler = handler;
	}

	@Override
	protected Object handleReq(Channel ctx, boolean isKeepAlive, Req req) throws Exception {
		return handler.call();
	}

}
