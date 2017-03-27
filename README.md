# dotCMS JS & CSS Minifier

Minifying JavaScript and CSS files and reducing the number of HTTP requests is [one of the most simple ways to speed up websites](http://developer.yahoo.com/performance/rules.html#num_http). After all: the fastest request is the request that way never made, and the smaller the file the faster it will be downloaded. This [dotCMS](https://www.dotcms.com/) plugin makes it easy to include these speed enhancements in your dotCMS web sites. The more CSS or JavaScript files you use on a page the bigger the performace gain will be.

Previous releases of this plugin for older dotCMS versions can be found [here](../../releases).

## Features

* Minify JavaScript
* Minify CSS
* Combine CSS and JS files from different VTL files
* Include files from other hosts
* Add inline JavaScript and CSS

## Installation

To use it, you obviously first need to install the plugin. Here's how you do that:

* Clone this repository.
* Open the console and go the the folder containing pom.xml
* Execute the following maven command: **mvn clean package**
* The build should succeed and a folder named "target" will be created.
* Open the "target" folder and check if the **.jar** file exists.
* Open dotCMS and go to the dotCMS Dynamic Plugins page by navigating to "System" > "Dynamic Plugins".
* Click on "Exported Packages" and add these packages to the list (make sure to add a comma to the last package in the existing list):

```java
com.dotmarketing.osgi,
com.dotmarketing.beans,
com.dotmarketing.plugin.business,
com.dotmarketing.portlets.fileassets.business,
com.dotmarketing.portlets.languagesmanager.business,
com.dotmarketing.portlets.languagesmanager.model,
com.liferay.portal
```

* Click on "Save Packages".
* Click on "Upload Plugin" and select the .jar file located in the "/target" folder.
* Click on "Upload Plugin" and the plugin should install and automatically start.
* To check if the plugin is working, visit the following URL and replace **your-url-here** with your own site address: **http://your-url-here/app/servlets/monitoring/isaac-dotcms-minifier**

That's it, you've installed the plugin. The plugin exposes two viewtools: **$jsMinifyTool** and **$cssMinifyTool** for working with JavaScript and CSS files respectively. In the [API](#api) section you can find all the details, but you can also read along to get a more step by step explanation of all its features.

## Usage

#### Minifying JavaScript - The basic case

Let's start with a simple case. Imagine you have two JavaScript files that are served from the current host and you'd like to minify and combine these into one call. Here's how that would work.

```
## For JavaScript files replace the script tags such as these two below...
 
<script src="/path/to/file1.js"></script>
<script src="/some/other/path/to/file2.js"></script>
 
## ...with a call to the viewtool like this (you can also place all the files 
## on the same line, but using a line per file results in a more readable result):
 
$jsMinifyTool.toScriptTag("
  /path/to/file1.js
  ,/some/other/path/to/file2.js
")
 
## The viewtool will generate one new script tag that would look like this:
 
<script src="/path/to/67057579813.minifier.js?uris=/path/to/file1.js,/some/other/path/to/file2.js"></script>

```
So we call the .toScriptTag() viewtool method with a string containing all the paths to the JavaScript files divided by commas. These paths must be relative to the root of the current host. This will generate a single HTML script tag whose "src" attribute will point to a servlet which is part of the plugin. Remember that it does not actually exist as a file in the dotCMS file system, it's just a servlet. This servlet will return one file that contains all of the content of all files only minified. The order in which the files are combined will be preserved. These files are actually mappings to servlets and do not actually exist as files in the dotCMS file system.

The digits in the URL are a so called "cache busting" technique. They are calculated by the plugin based on the modification date of each of the files, so when one of the files changes the digits will also change which results in a new URL. This will force the browser to rerequest the URL and not use its own cache. So using this plugin will not only speed up yours website it will also fix the issue with changed JavaScript and CSS files not being requested by the browser. To learn more about how this URL is constructed take a look at the .toUrl() method in the API section on this page.

#### Minifying CSS - The basic case

The basic CSS case is almost identical to the JavaScript case. All HTML link tags need to be removed and replaced by one viewtool method call. For example, suppose we have the following HTML link tags:

```
## For CSS files replace the link tags such as these two below...
 
<link href="/path/to/file1.css" rel="stylesheet" />
<link href="/path/to/file2.css" rel="stylesheet" />
 
## ...with a call to the viewtool like this:
 
$cssMinifyTool.toLinkTag("
  /path/to/file1.css
  ,/path/to/file2.css
")
 
## The viewtool will generate one new link tag that would look like this:
 
<link href="/path/to/65203502484.minifier.css?uris=/path/to/file1.css,/path/to/file2.css" rel="stylesheet" />
```

Notice the use of the $cssMinifyTool instead of $jsMinifyTool for CSS files. Also notice that the paths of the two files are the same in this example. Combining CSS files that are located in different directories can cause problems because CSS files often contain references to image files relative to their own location. A typical example would be: "background: transparent img(img/sprite.png) -23px -560px no-repeat". As you can see in the example above the path for the resulting link tag is "/path/to/" which the plugin determined by looking at the path of the first file in the list.

#### Combining CSS and JS files from different VTL files

Sometimes the existing script or link tags are not all in the same VTL file. You could of course remove the script tag from all the VTL files and group them in one special file and include that on the page, but that would make it hard to create components such as widgets that are self contained. Every time you would want to create a new widget that needs some additional script or CSS file you would have to add that one special file instead of just adding it to the code field of the widget. Having a call to .toScriptTag() or .toLinkTag() per widget would result in multiple script or link tags which is what we want to prevent. To fix this the plugin enables you to add JavaScript and CSS files to a named group which you can then later reference. Here's an example: let's say that you want to create a carousel widget that uses JavaScript to handle all the client side behavior. Inside the code field of that widget you can add the JavaScript file to a group and name that group. Here's how you'd do that:

```
## Add the JavaScript file to a group with name "groupName"
 
$jsMinifyTool.addFiles("
  /path/to/jQuery.js
  ,/some/other/path/to/carouselFile.js
", "groupName")
 
## At the location that you would want to have the actual script tag (for instance the footer 
## container) you can use the group in the normal .toScriptTag() like this:
 
$jsMinifyTool.toScriptTag("
  groupName
  ,/path/to/other/File.js
")
 
## The viewtool will generate one new link tag that would look like this:
 
<script src="/path/to/93726399672.minifier.js?uris=/path/to/jQuery.js,/some/other/path/to/carouselFile.js,/path/to/other/File.js"></script>
```

A you can see above the viewtool simply replaces the groupName with all the files that were added to the group. If two calls to .addFiles() add the same file to the same group it will be included only the first time. You can add as many groups as you like and use them in any order in any .toScriptTag() call. The CSS case works the same but uses the $cssMinifyTool.addFiles() viewtool method instead.

#### Including files from other hosts

Sometimes you have a additional host that host shared files that you want to include in the minified call as well. You can do this by providing a host object to the .toScriptTag(), .toLinkTag() and .addFiles() methods. All files provided to the methods will be retrieved from the given host. For example:

```
## Add three files from three different hosts (current host, $someHost and $anotherHost)
 
$jsMinifyTool.addFiles("/path/to/file.js", "groupName")
$jsMinifyTool.addFiles("/some/path/to/a/file.js", "groupName", $someHost)
$jsMinifyTool.toScriptTag("/path/to/otherFile.js, groupName", $anotherHost)
 
## This will result in this script tag:
 
<script src="/path/to/25963026436.minifier.js?uris=//anotherHost/path/to/otherFile.js,/path/to/file.js,//someHost/some/path/to/a/file.js"></script>
```

Notice the order in which the viewtool adds the files to the servlet call: the order in the .toScriptTag() call is leading. For security reasons you can only add files located on one of the hosts that are hosted on the same dotCMS server as the current host. Notice also that the methods expect a Host object and not a string, so to make it easy to retrieve the host the plugin provides a utility method: $jsMinifyTool.getHost("hostName or Alias"). The CSS case works the same but uses the $cssMinifyTool viewtool instead.

#### Add custom attributes to the generated script tag

If you want to make your script 'async' or 'defer', you have the option (since version 3.7.1) to use the to**Custom**ScriptTag:

```
$jsMinifyTool.toCustomScriptTag("groupName", "defer")
```
Since the extra string is added to the HTML this can be used to add "async", "defer", "defer async" and even "charset='UTF-8'".

#### Adding inline JavaScript and CSS

Because we can now redefine the location in the page where scripts are inluded we also need a way to move the location where the inline scripts that use those files are included. Here's an example:

```
## For an inline script replace the script tags such as in this example below...
 
<script>
  $("#carousel").carousel({
    speed: 100,
    showControls: true,
    title: "Our products"
  });
</script>
 
## ...with a call to the viewtool like this:
 
$jsMinifyTool.addInlines("
  $('#carousel').carousel({
    speed: 100,
    showControls: true,
    title: 'Our products'
  });
", "groupName")
 
## At the location in the page where you'd like this inline script to be put do this:  
 
#foreach ($i in $jsMinifyTool.getInlines("groupName")
  $i
#end
```

Notice that in the inline script the double quotes have been replaced by single quotes. This needs to be done to not interfere with the opening and closing double quotes of the string that is the parameter of the .addInlines() method. Instead of just adding one group as the parameter to the .getInlines() call you can add multiple groups in the string, separated by commas. The CSS case works the same but uses the $cssMinifyTool viewtool instead.

The examples above cover most of the use cases that you'll most likely need. See the [API](#api) section for all the details of these and some additional methods.


## Tips and Tricks

#### Debugging minification problems

There are a couple of things you should know about debugging minification issues:
* When you're developing a website and there's a bug in your CSS or JavaScript, you don't want to debug that issue using the minified version of the files. With the dotCMS minifier plugin you don't have to: when you add the parameter debug=true in the page url, the minify viewtool will generate the normal tags for you, which return the unminified files. For instance:
[http://mysite.com/home/index.html?debug=true](http://mysite.com/home/index.html?debug=true)
*  dotCMS encounters an error while minifying it will skip the file and include the unminified version instead. You can check the dotcms.log log file to see what went wrong.
* If one of the files cannot be found the viewools will throw an error in the HTML. So check the HTML file if your page does not seem to work correctly.

#### Limitations

There are currently two known limitations
* @import isn't minified.
* URLs in minified files can break.

here is no solution for the first limitation so far. However the usage of @import won't break your CSS, it will just not minify and combine the referenced @import files. So it is safe to use on your pages. Using @import to include CSS on your page has other issues that are unrelated to this plugin or even dotCMS, so try to prevent it.

There are two possible solutions for the second limitation. Let's first explain the problem further. When you return all the CSS from one file, that means that all relative URLs are determined by looking at that single file. This is no problem when all your CSS files are in the same directory, it becomes a problem when that isn't the case. Say we have two css files /css/style.css which refers to img/style.jpg, and /extra/extra.css which refers to img/extra.jpg. When both files are minified and returned from /css/7876862326.minifier.css, the img/extra.jpg can't be found (because it's at /extra/img/extra.jpg and not /css/img/extra.jpg). This is a problem.

To solve this you can do three things:

* Use URLs relative to the root, so /css/img/style.jpg and /extra/img/extra.jpg
* Put your CSS in the same directory, since the minifier.css is called from the directory of the first URI in the viewtool.
* Use a .toLinkTag() per directory. This will increase the number of calls but the CSS will still be minified.

#### About the minification process

Files that have the string ".min" in their filename will not be minified. The plugins assumes that these files have already been minified. For minifying JavaScript files the plugin uses the [Google closure compiler](https://developers.google.com/closure/compiler), for minifying CSS files [YUI Compressor](http://yui.github.io/yuicompressor) is used. In both cases only newlines, comments and unneccessary whitespace is removed. We have found that these settings causes the least minification issues.

#### DotCMS cache

The plugin does not minify a file each time it is called, but stores the minified result in the existing dotCMS cache. By using this dotCMS cache, you have control over the minified files. When you want to remove the cached minified files, just flush the dotCMS cache and you're done. The cache will be re-filled on-the-fly. When you change the file through the admin interface or through WEBDAV this cache will be automatically flushed.

## <a name="api"></a>API

The API can be found [here](https://github.com/isaaceindhoven/DotCMS-Minifier/wiki/API-Documentation).

## Meta

[ISAAC - 100% Handcrafted Internet Solutions](https://www.isaac.nl) – [@ISAAC](https://twitter.com/isaaceindhoven) – [info@isaac.nl](mailto:info@isaac.nl)

Distributed under the [Creative Commons Attribution 3.0 Unported License](https://creativecommons.org/licenses/by/3.0/).
