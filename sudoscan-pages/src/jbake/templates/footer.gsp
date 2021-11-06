		</div>
		<div id="push"></div>
    </div>

    <div id="footer">
      <div class="container">
        <p class="muted credit">All posts on this blog are published with a <em>Creative Commons by-nc-sa</em> license.<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/2.0/fr/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nc-sa/2.0/fr/88x31.png"/></a></p>
        <p class="muted credit">&copy; 2021 | Mixed with <a href="http://twitter.github.com/bootstrap/">Bootstrap v3.0.3</a> | Baked with <a href="http://jbake.org">JBake ${version}</a></p>
      </div>
    </div>

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/jquery-1.11.1.min.js"></script>
    <script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/bootstrap.min.js"></script>
    <script src="<%if (content.rootpath) {%>${content.rootpath}<% } else { %><% }%>js/prettify.js"></script>

    <!-- Global site tag (gtag.js) - Google Analytics -->
    <script async src="https://www.googletagmanager.com/gtag/js?id=G-0DNWDTGYP7"></script>
    <script>
      window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());

      gtag('config', 'G-0DNWDTGYP7');
    </script>

  </body>
</html>
