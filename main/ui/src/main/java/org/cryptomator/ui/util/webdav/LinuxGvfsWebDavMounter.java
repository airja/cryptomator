/*******************************************************************************
 * Copyright (c) 2014 Sebastian Stenzel, Markus Kreusch
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 * 
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *     Markus Kreusch - Refactored WebDavMounter to use strategy pattern
 ******************************************************************************/
package org.cryptomator.ui.util.webdav;

import java.net.URI;

import org.apache.commons.lang3.SystemUtils;
import org.cryptomator.ui.util.command.Script;

final class LinuxGvfsWebDavMounter implements WebDavMounterStrategy {

	@Override
	public boolean shouldWork() {
		if (SystemUtils.IS_OS_LINUX) {
			final Script checkScripts = Script.fromLines("which gvfs-mount xdg-open");
			try {
				checkScripts.execute().assertOk();
				return true;
			} catch (CommandFailedException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public WebDavMount mount(final URI uri) throws CommandFailedException {
		final Script mountScript = Script.fromLines(
				"set -x",
				"gvfs-mount \"$URI\"",
				"xdg-open \"$URI\"")
				.addEnv("URI", uri.toString());
		final Script unmountScript = Script.fromLines(
				"set -x",
				"gvfs-mount -u \"$URI\"")
				.addEnv("URI", uri.toString());
		mountScript.execute().assertOk();
		return new WebDavMount() {
			@Override
			public void unmount() throws CommandFailedException {
				unmountScript.execute().assertOk();
			}
		};
	}

}
