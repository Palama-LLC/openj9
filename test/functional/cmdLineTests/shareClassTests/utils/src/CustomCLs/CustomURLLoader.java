/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corp. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set
 * forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
 * General Public License, version 2 with the GNU Classpath
 * Exception [1] and GNU General Public License, version 2 with the
 * OpenJDK Assembly Exception [2].
 *
 * [1] https://www.gnu.org/software/classpath/license.html
 * [2] https://openjdk.org/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 OR LicenseRef-GPL-2.0 WITH Assembly-exception
 *******************************************************************************/
package CustomCLs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.util.Hashtable;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import com.ibm.oti.shared.HelperAlreadyDefinedException;
import com.ibm.oti.shared.Shared;
import com.ibm.oti.shared.SharedClassHelperFactory;
import com.ibm.oti.shared.SharedClassTokenHelper;
import com.ibm.oti.shared.SharedClassURLClasspathHelper;
import com.ibm.oti.shared.SharedClassURLHelper;

/**
 * @author Matthew Kilner
 */
public class CustomURLLoader extends SecureClassLoader {

	URL[] urls, orgUrls, searchUrls;
	URL url;
	
	private Hashtable jarCache = new Hashtable(32);
	int loaderType;
	
	SharedClassURLHelper scHelper;
	
	CustomLoaderMetaDataCache[] metaDataArray;

	public CustomURLLoader(URL[] passedUrls, ClassLoader parent){
		super(parent);
		loaderType = ClassLoaderType.CACHEDURL.ord;
		int urlLength = passedUrls.length;
		searchUrls = new URL[urlLength];
		urls = new URL[1];
		orgUrls = new URL[urlLength];
		for (int i=0; i < urlLength; i++) {
			try {
				searchUrls[i] = createSearchURL(passedUrls[i]);
			} catch (MalformedURLException e) {}
			orgUrls[i] = passedUrls[i];
		}
		metaDataArray = new CustomLoaderMetaDataCache[searchUrls.length];
		initMetaData();
		SharedClassHelperFactory schFactory = Shared.getSharedClassHelperFactory();
		if(schFactory != null){
			try{
				scHelper = schFactory.getURLHelper(this);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public CustomURLLoader(URL[] passedUrls){
		super();
		loaderType = ClassLoaderType.CACHEDURL.ord;
		int urlLength = passedUrls.length;
		searchUrls = new URL[urlLength];
		urls = new URL[1];
		orgUrls = new URL[urlLength];
		for (int i=0; i < urlLength; i++) {
			try {
				searchUrls[i] = createSearchURL(passedUrls[i]);
			} catch (MalformedURLException e) {}
			orgUrls[i] = passedUrls[i];
		}
		metaDataArray = new CustomLoaderMetaDataCache[searchUrls.length];
		initMetaData();
		SharedClassHelperFactory schFactory = Shared.getSharedClassHelperFactory();
		if(schFactory != null){
			try{
				scHelper = schFactory.getURLHelper(this);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void loadClassFrom(String name, int index){
		urls[0] = searchUrls[index];
		url = orgUrls[index];
		try{
			if (this.loadClass(name)==null)
			{
				System.out.println("CustomeURLLoader::loadClassFrom returned null.");
			}
		} catch(ClassNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public void loadClassNullStore(String name){
		urls[0] = searchUrls[0];
		int indexFoundAt = locateClass(name);
		try{
			byte[] classBytes = loadClassBytes(name, indexFoundAt);
			checkPackage(name, indexFoundAt);
			CustomLoaderMetaDataCache metadata = metaDataArray[indexFoundAt];
			CodeSource codeSource = metadata.codeSource;
			Class clazz = defineClass(name, classBytes, 0, classBytes.length, codeSource);
			if(clazz != null){
				scHelper.storeSharedClass(null, clazz);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean getHelper(){
		SharedClassHelperFactory schFactory = Shared.getSharedClassHelperFactory();
		SharedClassURLHelper newHelper = null;
		try{
			newHelper = schFactory.getURLHelper(this);
		} catch (Exception e){
			return false;
		}
		
		if(newHelper.equals(scHelper)){
			return true;
		} else {
			return false;
		}
	}
	
	public void getURLClasspathHelper()throws HelperAlreadyDefinedException {
		SharedClassHelperFactory schFactory = Shared.getSharedClassHelperFactory();
		SharedClassURLClasspathHelper newHelper = null;
		newHelper = schFactory.getURLClasspathHelper(this, urls);
	}
	
	public void getTokenHelper()throws Exception {
		SharedClassHelperFactory schFactory = Shared.getSharedClassHelperFactory();
		SharedClassTokenHelper newHelper = null;
		newHelper = schFactory.getTokenHelper(this);
	}
	
	public boolean duplicateStore(String name){
		Class clazz = null;
		try{
			clazz = this.loadClass(name);	
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		}
		if (clazz != null){
			int indexFoundAt = locateClass(name);
			if(indexFoundAt != -1){
				scHelper.storeSharedClass(urls[indexFoundAt], clazz);
				return true;
			}
		}
		return false;
	}
	
	private void initMetaData(){
		for(int loopIndex = 0; loopIndex < metaDataArray.length; loopIndex++){
			metaDataArray[loopIndex] = null;
		}
	}
	
	private URL createSearchURL(URL url) throws MalformedURLException {
		if (url == null) return url;
		String protocol = url.getProtocol();

		if (isDirectory(url) || protocol.equals("jar")) {
			return url;
		} else {
				return new URL("jar", "", -1, url.toString() + "!/");
		}
	}
	
	private static boolean isDirectory(URL url) {
		String file = url.getFile();
		return (file.length() > 0 && file.charAt(file.length()-1) == '/');
	}
	
	public Class findClass(String name) throws ClassNotFoundException {
		Class clazz = null;
		int indexFoundAt = locateClass(name);
		if(scHelper != null){
			byte[] classBytes = scHelper.findSharedClass(url, name);
			if(classBytes != null){
				if(metaDataArray[indexFoundAt] != null){
					checkPackage(name, indexFoundAt);
					CustomLoaderMetaDataCache metadata = metaDataArray[indexFoundAt];
					CodeSource codeSource = metadata.codeSource;
					clazz = defineClass(name, classBytes, 0, classBytes.length, codeSource);
				}
			}
		}
		if(clazz == null) {
			if(indexFoundAt != -1){
				try{
					byte[] classBytes = loadClassBytes(name, indexFoundAt);
					checkPackage(name, indexFoundAt);
					CustomLoaderMetaDataCache metadata = metaDataArray[indexFoundAt];
					CodeSource codeSource = metadata.codeSource;
					clazz = defineClass(name, classBytes, 0, classBytes.length, codeSource);
					if(clazz != null){
						scHelper.storeSharedClass(url, clazz);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			if(clazz == null){
				throw new ClassNotFoundException(name);
			}
		}
		return clazz;
	}
	
	private void checkPackage(String name, int urlIndex){
		int index = name.lastIndexOf('.');
		if(index != -1){
			String packageString = name.substring(0, index);
			Manifest manifest = metaDataArray[urlIndex].manifest;
			CodeSource codeSource = metaDataArray[urlIndex].codeSource;
			synchronized(this){
				Package packageInst = getPackage(packageString);
				if(packageInst == null){
					if (manifest != null){
						definePackage(packageString, manifest, codeSource.getLocation());
					} else {
						definePackage(packageString, null, null, null, null, null, null, null);
					}
				} else {
					boolean exception = false;
					if (manifest != null) {
						String dirName = packageString.replace('.', '/') + "/";
						if (isSealed(manifest, dirName))
							exception = !packageInst.isSealed(codeSource.getLocation());
					} else
						exception = packageInst.isSealed();
					if (exception)
						throw new SecurityException(com.ibm.oti.util.Msg.getString("Package exception"));
				}	
			}
		}		
	}
	
	private static byte[] getBytes(InputStream is, boolean readAvailable) throws IOException {
		if (readAvailable) {
			byte[] buf = new byte[is.available()];
			is.read(buf, 0, buf.length);
			is.close();
			return buf;
		}
		byte[] buf = new byte[4096];
		int size = is.available();
		if (size < 1024) size = 1024;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		int count;
		while ((count = is.read(buf)) > 0)
			bos.write(buf, 0, count);
		return bos.toByteArray();
	}
	
	private byte[] loadClassBytes(String className, int urlIndex){
		byte[] bytes = null;
		String name = className.replace('.','/').concat(".class");
		URL classLocation = urls[urlIndex];
		if(classLocation.getProtocol().equals("jar")){
			JarFile jf = (JarFile)jarCache.get(classLocation);
			JarEntry entry = jf.getJarEntry(name);
			try{
				InputStream stream = jf.getInputStream(entry);
				bytes = getBytes(stream, true);
			} catch (Exception e){
				e.printStackTrace();
			}
		} else {
			String filename = classLocation.getFile();
			String host = classLocation.getHost();
			if (host != null && host.length() > 0) {
				filename = new StringBuffer(host.length() + filename.length() + name.length() + 2).
					append("//").append(host).append(filename).append(name).toString();
			} else {
				filename = new StringBuffer(filename.length() + name.length()).
					append(filename).append(name).toString();
			}
			File file = new File(filename);
			// Don't throw exceptions for speed
			if (file.exists()) {
				try{
					FileInputStream stream = new FileInputStream(file);
					bytes = getBytes(stream, true);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}
	
	private int locateClass(String className){
		int classAtEntry = -1;
		String name = className.replace('.','/').concat(".class");		
		for(int index = 0; index < urls.length; index ++){
			URL currentUrl = urls[index];
			if(currentUrl.getProtocol().equals("jar")){
				JarEntry entry = null;
				JarFile jf = (JarFile)jarCache.get(currentUrl);
				if(jf == null){
					/* First time we have encountered this jar.
					 * Lets cache its metaData.
					 */
					try{
						URL jarFileUrl = ((JarURLConnection)currentUrl.openConnection()).getJarFileURL();
						JarURLConnection jarUrlConnection = (JarURLConnection)new URL("jar", "", jarFileUrl.toExternalForm() + "!/").openConnection();
						try{
							jf = jarUrlConnection.getJarFile();
						}catch(Exception e){
						}
						if(jf != null){
							jarCache.put(currentUrl, jf);
						}
					} catch (Exception e){
						e.printStackTrace();
					}
					if(jf != null){
						Manifest manifest = null;
						java.security.cert.Certificate[] certs = null;
						URL csUrl = currentUrl;
						CodeSource codeSource;
						try{
							manifest = jf.getManifest();
						} catch(Exception e){
							e.printStackTrace();
						}
						entry = jf.getJarEntry(name);
						if(entry != null){
							certs = entry.getCertificates();
						}
						codeSource = new CodeSource(csUrl, certs);
						CustomLoaderMetaDataCache metaData = new CustomLoaderMetaDataCache();
						metaData.manifest = manifest;
						metaData.codeSource = codeSource;
						metaDataArray[index] = metaData;
					}
				}
				if(entry == null && jf != null){
					entry = jf.getJarEntry(name);
				}
				if(entry != null){
					/* We have the first match on the class path, return the current url index */
					return index;
				}
			} else {
				String filename = currentUrl.getFile();
				String host = currentUrl.getHost();
				if (host != null && host.length() > 0) {
					filename = new StringBuffer(host.length() + filename.length() + name.length() + 2).
						append("//").append(host).append(filename).append(name).toString();
				} else {
					filename = new StringBuffer(filename.length() + name.length()).
						append(filename).append(name).toString();
				}
				File file = new File(filename);
				// Don't throw exceptions for speed
				if (file.exists()) {
					if(metaDataArray[index] == null){
						java.security.cert.Certificate[] certs = null;
						CustomLoaderMetaDataCache metaData = new CustomLoaderMetaDataCache();
						metaData.manifest = null;
						metaData.codeSource = new CodeSource(currentUrl, certs);
						metaDataArray[index] = metaData;
					}
					return index;
				}
			}			
		}
		return classAtEntry;
	}
	
	private boolean isSealed(Manifest manifest, String dirName) {
		Attributes mainAttributes = manifest.getMainAttributes();
		String value = mainAttributes.getValue(Attributes.Name.SEALED);
		boolean sealed = value != null &&
			value.toLowerCase().equals ("true");
		Attributes attributes = manifest.getAttributes(dirName);
		if (attributes != null) {
			value = attributes.getValue(Attributes.Name.SEALED);
			if (value != null)
				sealed = value.toLowerCase().equals("true");
		}
		return sealed;
	}
	
	protected Package definePackage(String name, Manifest man, URL url) throws IllegalArgumentException {
		String path = name.replace('.','/').concat("/");
		String[] attrs = new String[7];
		URL sealedAtURL = null;
		
		Attributes attr = man.getAttributes(path);
		Attributes mainAttr = man.getMainAttributes();
		
		if(attr != null){
			attrs[0] = attr.getValue(Name.SPECIFICATION_TITLE);
			attrs[1] = attr.getValue(Name.SPECIFICATION_VERSION);
			attrs[2] = attr.getValue(Name.SPECIFICATION_VENDOR);
			attrs[3] = attr.getValue(Name.IMPLEMENTATION_TITLE);
			attrs[4] = attr.getValue(Name.IMPLEMENTATION_VERSION);
			attrs[5] = attr.getValue(Name.IMPLEMENTATION_VENDOR);
			attrs[6] = attr.getValue(Name.SEALED);
		}
		
		if(mainAttr != null){
			if (attrs[0] == null) {
				attrs[0] = mainAttr.getValue(Name.SPECIFICATION_TITLE);
			}
			if (attrs[1] == null) {
				attrs[1] = mainAttr.getValue(Name.SPECIFICATION_VERSION);
			}
			if (attrs[2] == null) {
				attrs[2] = mainAttr.getValue(Name.SPECIFICATION_VENDOR);
			}
			if (attrs[3] == null) {
				attrs[3] = mainAttr.getValue(Name.IMPLEMENTATION_TITLE);
			}
			if (attrs[4] == null) {
				attrs[4] = mainAttr.getValue(Name.IMPLEMENTATION_VERSION);
			}
			if (attrs[5] == null) {
				attrs[5] = mainAttr.getValue(Name.IMPLEMENTATION_VENDOR);
			}
			if (attrs[6] == null) {
				attrs[6] = mainAttr.getValue(Name.SEALED);
			}
		}
		if ("true".equalsIgnoreCase(attrs[6])){
			sealedAtURL = url;
		}
		return definePackage(name, attrs[0], attrs[1], attrs[2], attrs[3], attrs[4], attrs[5], sealedAtURL);
	}
	
	public boolean isClassInSharedCache(int index, String className){
		URL url = orgUrls[index];
		byte[] sharedClass = null;
		if (scHelper!=null) {
			sharedClass = scHelper.findSharedClass(url, className);
			if (sharedClass !=null){
				return true;
			} else {
				return false;
			}
		}
		System.out.println("CustomeURLLoader::isClassInSharedCache scHelper is null.");
		return false;
	}
	
	public boolean isClassInSharedCacheNullFind(String className){
		byte[] sharedClass = null;
		if (scHelper!=null) {
			sharedClass = scHelper.findSharedClass(null, className);
			if (sharedClass !=null){
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
