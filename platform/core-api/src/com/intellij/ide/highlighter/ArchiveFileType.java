/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.ide.highlighter;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ArchiveFileType implements FileType {
  private static final NotNullLazyValue<Icon> ICON = new NotNullLazyValue<Icon>() {
      @NotNull
      @Override
      protected Icon compute() {
        return AllIcons.FileTypes.Archive;
      }
    };

  public static final ArchiveFileType INSTANCE = new ArchiveFileType();

  @NotNull
  public String getName() {
    return "ARCHIVE";
  }

  @NotNull
  public String getDescription() {
    return IdeBundle.message("filetype.description.archive.files");
  }

  @NotNull
  public String getDefaultExtension() {
    return "";
  }

  public Icon getIcon() {
    return ICON.getValue();
  }

  public boolean isBinary() {
    return true;
  }

  public boolean isReadOnly() {
    return false;
  }

  public String getCharset(@NotNull VirtualFile file, final byte[] content) {
    return null;
  }
}
