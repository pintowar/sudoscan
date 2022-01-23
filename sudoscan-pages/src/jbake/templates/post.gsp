<%include "header.gsp"%>

	<%include "menu.gsp"%>

	<div class="page-header">
		<h1>${content.title}</h1>
	</div>

	<p><em>${content.date.format("dd MMMM yyyy")}</em></p>

	<p>${content.body}</p>

	<hr />

	<div class="row">
        <div class="col-md-6">
            <a href="${content.previous ? (content.previous + '.html') : '#'}">${content.previous ? '<< prev' : ''}</a>
        </div>
        <div class="col-md-6 text-right">
            <a href="${content.next ? (content.next + '.html') : '#'}">${content.next ? 'next >>' : ''}</a>
        </div>
    </div>

<%include "footer.gsp"%>
