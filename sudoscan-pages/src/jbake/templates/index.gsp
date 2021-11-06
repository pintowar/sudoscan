<%include "header.gsp"%>

	<%include "menu.gsp"%>

	<div class="page-header">
        <div class="row">
            <div class="col-xs-12 col-md-8"><h1>Sudoscan Pages</h1></div>
        </div>
	</div>

    <div class="row">

        <div class="col-sm-8">

            <% posts.take(8).each { post -> %>
                <%if (post.status == "published") {%>
                    <div  itemscope itemtype="http://schema.org/Blog">
                        <div itemprop="author" itemscope itemtype="http://schema.org/Person">
                            <meta itemprop="name" content="Yan Bonnel"/>
                        </div>
                        <meta itemprop="inLanguage" content="en-US"/>
                        <a itemprop="url" href="${post.uri}">
                            <h1 itemprop="name">${post.title}</h1>
                        </a>
                        <p>
                            <time itemprop="datePublished"
                                  datetime="${post.date.format("yyyy-MM-dd")}">
                                ${post.date.format("dd MMMM yyyy")}
                            </time>
                             - Tags :
                            <meta itemprop="keywords" content="${post.tags.join(",")}"/>
                            <%
                                out << post.tags.collect { post_tag ->
                                    """<a href="tags/${post_tag}.html">${post_tag}</a>"""
                                } .join(", ")
                            %>
                        </p>

                        <%indexBody = post.body.indexOf("REPLACE_WITH_READ_MORE")
                          if ( indexBody > 0) {%>

                        <div itemprop="blogPost">
                            <p>${post.body.substring(0, indexBody)}</p>
                            <p><a itemprop="readMore" href="${post.uri}">Read more</a></p>
                        </div>
                        <%}else{%>
                        <div itemprop="blogPost">
                            <p>${post.body}</p>
                        </div>
                        <%}%>
                    </div>
                <%}%>

            <%}%>

            <hr />

        </div>


    </div>

<%include "footer.gsp"%>
