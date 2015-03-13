/*******************************************************************************
 * Copyright (c) 2015 pf-miles.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     pf-miles - initial API and implementation
 ******************************************************************************/
package com.github.pfmiles.kanjava.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * A compiled java class file in memory.
 * 
 * @author pf-miles 2014-4-3 下午3:20:03
 */
public class JavaClassFile extends SimpleJavaFileObject {

    private String fileName;
    private String binaryName;
    private byte[] data;

    public JavaClassFile(String fileName, String pkg, byte[] data) {
        super(genMemFileUri(fileName, pkg), Kind.CLASS);
        this.fileName = fileName;
        this.data = data;
        if (DynaCompileUtil.isNotBlank(pkg)) {
            this.binaryName = pkg + "." + DynaCompileUtil.substringBeforeLast(fileName, ".");
        } else {
            this.binaryName = DynaCompileUtil.substringBeforeLast(fileName, ".");
        }
    }

    private static URI genMemFileUri(String fileName, String pkg) {
        String pkgPath = "";
        if (DynaCompileUtil.isNotBlank(pkg))
            pkgPath = pkg.replaceAll("\\.", "/") + "/";
        return URI.create("mem:///target/" + pkgPath + fileName);
    }

    public String getName() {
        return this.fileName;
    }

    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(this.data);
    }

    public OutputStream openOutputStream() throws IOException {
        return new ByteArrayOutputStream() {

            public void close() throws IOException {
                super.close();
                JavaClassFile.this.data = this.toByteArray();
            }
        };
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Writer openWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean delete() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return uri.toString();
    }

    public String getBinaryClassName() {
        return this.binaryName;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JavaClassFile other = (JavaClassFile) obj;
        if (uri == null) {
            if (other.toUri() != null)
                return false;
        } else if (!uri.equals(other.toUri()))
            return false;
        return true;
    }

    public byte[] getData() {
        return data;
    }
}
