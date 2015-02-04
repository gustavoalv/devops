/**
 * 
 */
package co.com.extensions.util;

/**
 * @author alvgu02
 *
 */
public class ElementPS9 {
	
	 private String tagOnly;// <tag a ="b" c= 'd' e=f> contentssss </tag>
     
     private String tagname;// tag
     
     private String attributes;// a ="b" c= 'd' e=f
     
     private String content;// contentssss

	/**
	 * @return the tagOnly
	 */
	public String getTagOnly() {
		return tagOnly;
	}

	/**
	 * @param tagOnly the tagOnly to set
	 */
	public void setTagOnly(String tagOnly) {
		this.tagOnly = tagOnly;
	}

	/**
	 * @return the tagname
	 */
	public String getTagname() {
		return tagname;
	}

	/**
	 * @param tagname the tagname to set
	 */
	public void setTagname(String tagname) {
		this.tagname = tagname;
	}

	/**
	 * @return the attributes
	 */
	public String getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
     
     

}
