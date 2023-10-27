
(function (global) {
	RenderObject = {
		render: function (template, model, ctx) {

			var url = ctx.url;
			// Find the last forward slash
			var lastSlashIndex = url.lastIndexOf('/');

			// Find the second-to-last forward slash by searching before the lastSlashIndex
			var secondLastSlashIndex = url.lastIndexOf('/', lastSlashIndex - 1);

			var tpl_type;

			if (secondLastSlashIndex !== -1) {
				tpl_type = url.substring(secondLastSlashIndex + 1, lastSlashIndex);
			} else {
				return "No template engine found for " + path + "!";
			}

			switch (tpl_type) {
				// case match /ejs/:
				case "ejs":
					return ejs.render(template, model);
				case "mustache":
					return Mustache.render(template, model);
				default:
					return "No template engine found for " + tpl_type + "!";
			}
			// return Object.keys(global).join(", ") + ",," + (typeof ctx) + "," + ctx.url;
		}
	}
	global["RenderObject"] = RenderObject;
})(this);