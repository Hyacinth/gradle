/*
 * Copyright 2013 the original author or authors.
 *
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
 */

package org.gradle.api.internal.changedetection.rules;

import org.gradle.api.Task;
import org.gradle.api.internal.changedetection.ChangeType;
import org.gradle.api.internal.changedetection.TaskUpToDateStateChange;

import java.io.File;

public class FileChange implements TaskUpToDateStateChange {
    private final Task task;
    private final File file;
    private final String fileType;
    private final ChangeType change;

    public FileChange(Task task, File file, String fileType, ChangeType change) {
        this.task = task;
        this.file = file;
        this.fileType = fileType;
        this.change = change;
    }

    public String getMessage() {
        return String.format("%s file %s for %s %s.", fileType, file, task, change.describe());
    }

    public ChangeType getChange() {
        return change;
    }

    public File getFile() {
        return file;
    }
}
