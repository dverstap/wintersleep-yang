/*-
 * #%L
 * org.wintersleep.yang:wintersleep-yang-bbf
 * %%
 * Copyright (C) 2017 - 2018 Davy Verstappen
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
package org.wintersleep.yang.schema

import java.io.File
import java.nio.file.Paths
import java.util.*

object YangTools {

    fun findYangFiles(parent: File): Set<File> {
        val result = TreeSet<File>()
        if (!parent.exists()) {
            throw IllegalArgumentException("Parent does not exist: $parent")
        }
        if (parent.isDirectory) {
            for (child in parent.listFiles()) {
                result.addAll(findYangFiles(child))
            }
        } else if (parent.isFile and parent.name.endsWith(".yang")) {
            result.add(parent)
        }
        return result
    }

    // Really a generic test function
    fun findMyModuleTopDir(myModuleName: String): File {
        val pwd = Paths.get("").toAbsolutePath()
        if (pwd.endsWith(Paths.get(myModuleName))) {
            return pwd.toFile()
        }
        return pwd.resolve(myModuleName).toFile()
    }

}
