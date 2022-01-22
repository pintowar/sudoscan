<!-- Fixed navbar -->
<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a href="<% content.rootpath ?: '' %>index.html" class="navbar-brand">Sudoscan</a>
        </div>

        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li>
                    <a href="<% content.rootpath ?: '' %>intro.html">Intro</a>
                </li>
                <li>
                    <a href="<% content.rootpath ?: '' %>engine.html">Engine & Cli</a>
                </li>
                <li>
                    <a href="<% content.rootpath ?: '' %>extractor.html">Extractor & Plotter</a>
                </li>
                <li>
                    <a href="<% content.rootpath ?: '' %>recognizer.html">Recognizer</a>
                </li>
                <li>
                    <a href="<% content.rootpath ?: '' %>solver.html">Solver</a>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
              <li><a href="https://github.com/pintowar"><i class="fa fa-github"></i></a></li>
              <li><a href="https://twitter.com/pintowar"><i class="fa fa-twitter"></i></a>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</div>
<div class="container">
