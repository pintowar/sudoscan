<%include "header.gsp"%>

	<%include "menu.gsp"%>

	<div class="page-header">
        <div class="row">
            <div class="col-xs-12 col-md-8"><h1>Sudoscan Project</h1></div>
        </div>
	</div>

    <div class="row">
        <div class="col-sm-8">
            <p>A quick view on Sudoscan in action can be seen in this video:</p>
        </div>
    </div>

    <div class="row">
        <div class="col-sm-8 col-sm-offset-2">
            <div class="videoblock text-center">
                <div class="content">
                    <iframe width="640" height="480" src="https://www.youtube.com/embed/8D4gMhDRu-U?rel=0"
                        frameborder="0" allowfullscreen=""></iframe>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-sm-8">
            <% map = posts.collectEntries { post -> [post.title, post] } %>
            <% order = ['Intro', 'Engine & Cli', 'Extractor & Plotter', 'Recognizer', 'Solver'] %>

            <p>
                This blog contains a series of posts related to the creation of the Sudoscan Project.
                The posts should be read in the following order:
            </p>

            <ol>
                <% order.each { title -> %>
                    <li>
                        <a itemprop="url" href="${map[title].uri}">${title}</a>
                    </li>
                <%}%>
            </ol>
        </div>
    </div>

<%include "footer.gsp"%>
