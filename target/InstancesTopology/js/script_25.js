$('document')
		.ready(
				function() {
					var topologyToBeCalled = isCanTalkOn ? 'GenericTopology'
							: 'SpecificTopology';
					$.get(topologyToBeCalled, function(responseJson) {
						data = responseJson;
						drawGraph(data);
					});
					function drawGraph(json) {
						// doc width
						var width = $(document).width();
						width = 1200;

						// doc height
						var height = 700;

						var fill = d3.scale.category20();
						// layout of data
						var force = d3.layout.force().charge(-1030)
								.linkStrength(0.1).theta(.4).gravity(.234)
								.linkDistance(600).nodes(json.nodes).links(
										json.links).size([ width, height ])
								.start();
						var k = 0;
						while ((force.alpha() > 1e-2) && (k < 150)) {
							force.tick(), k = k + 1;
						}

						// add default html structure
						var svg = d3.select("#graph").append("svg").attr(
								"width", width).attr("height", height).attr(
								"xmlns", "ttp://www.w3.org/2000/svg").attr(
								"pointer-events", "all").append("svg:g")
								.call(
										d3.behavior.zoom([ 10, 15 ]).on("zoom",
												redraw)).append("svg:g");

						svg.append("svg:rect").attr("width", width).attr(
								"height", height).attr("fill", 'transparent')
								.on('mousedown', function() {
									d3.select(this).attr('class', 'drag');
								}).on('mouseup', function() {
									d3.select(this).attr('class', '');
								})

						// add links and style links
						var link = svg.selectAll(".gLink").data(json.links)
								.enter().append("g").attr("class", "gLink")
								.append("line").attr("class", "link").style(
										"stroke", "#000").style("stroke-width",
										function(d) {
											return d.weight;
										})

						// add nodes and effects
						var node = svg.selectAll("g.node").data(json.nodes)
								.enter().append("g").attr("class", "node")
								.style("z-index", "100").attr("cursor",
										"pointer").attr(
										"transform",
										function(d) {
											return "translate(" + d.x + ","
													+ d.y + ")";
										}).call(force.drag);

						var drag = force.drag().on("dragstart", dragstart);

						function dragstart(d, i) {
							force.stop();
						}

						var linkText = svg.selectAll(".gLink").append(
								"svg:marker").attr("id", 'String').attr("refX",
								0).attr("refY", 0).attr("markerWidth", 0).attr(
								"markerHeight", 0).attr("orient", "auto");

						var foreignObject = node
								.append("svg:foreignObject")
								.attr("x", function(d) {
									return -1 * (d.name.length * 7) / 2
								})
								.attr("y", -5)
								.attr("rx", 25)
								.attr("ry", 25)
								.attr("width", 88)
								.attr("height", 40)
								.append("xhtml:div")
								.attr('class', 'node zoom2')
								.html(
										"<div class='ipWrapper'><span class='ipAdd'></span><span class='states'><span class='nodeIndicator state red'></span><span class='nodeIndicator state green'></span></span></div><div class='nodeActions'><span class='menuItems'><i class='icon-align-justify'></i></span><span class='legend'>M</span></div>");

						$(data.nodes).each(function(e) {
							$('.ipAdd').eq(e).append(data.nodes[e].name);
						})

						function redraw() {
							svg.attr("transform", "translate("
									+ d3.event.translate + ")" + " scale("
									+ d3.event.scale + ")");
						}

						// click and drag animation
						force.on("tick", function() {
							link.attr("x1", function(d) {
								return d.source.x;
							}).attr("y1", function(d) {
								return d.source.y;
							}).attr("x2", function(d) {
								return d.target.x;
							}).attr("y2", function(d) {
								return d.target.y;
							});

							node.attr("transform", function(d) {
								return "translate(" + d.x + "," + d.y + ")";
							});

							linkText.attr("refX", 0).attr("refY", -0).attr(
									"markerWidth", 9).attr("markerHeight", 9)

							force.stop();
						});

						$('#loading-overlay').fadeOut();

						function fade(opacity, x) {
							return function(d) {
								node.style("stroke-opacity", function(o) {
									console.log(d + ' ' + o);
									thisOpacity = isConnected(d, o) ? 1
											: opacity;
									this.setAttribute('fill-opacity',
											thisOpacity);
									return thisOpacity;
								});

								link.style("stroke-opacity", opacity).style(
										"stroke-opacity",
										function(o) {
											return o.source === d
													|| o.target === d ? 1
													: opacity;
										});
							};
						}

						$('.searchNode').on('click', function() {
							var getSearchVal = $('.searchInput').val();
							if (getSearchVal.length > 0 || getSearchVal != '') {
								searchNode(getSearchVal);
							} else {
								alert('nothing is mentioned');
							}
						})

						var nodeIPAdd = $('.ipAdd');

						shortcut.add("Enter", function() {
							var getSearchVal = $('.searchInput').val();
							if (getSearchVal.length > 0 || getSearchVal != '') {
								searchNode(getSearchVal);
							} else {
								alert('nothing is mentioned');
							}
						});

						shortcut.add("Ctrl+F", function() {
							$('.searchWrapper').fadeIn("slow");
							$('.searchInput').val('');
							$('.searchInput').focus();
							unBindEvent();
						});

						shortcut.add("Esc", function() {
							clearResult();
							bindEvents();
							$('.searchWrapper').fadeOut("slow");
						});

						bindEvents();

						function getThisNodeText() {
							var getThisText = $(this).text();
							searchNode(getThisText);
						}

						function unBindEvent() {
							$(nodeIPAdd).unbind("mouseover", getThisNodeText)
							$(nodeIPAdd).unbind("click", getThisNodeText);
							$(nodeIPAdd).unbind("mouseout", clearResult);
						}

						function bindEvents() {
							$(nodeIPAdd).on("mouseover", getThisNodeText);
							$(nodeIPAdd).on("mouseout", clearResult);
						}

						function clearResult() {
							$('[data="activeNodes"]').attr('class', '');
							$('g[data="selected"]').attr('data', '');
							$('g[data="default"]').attr('data', '');
						}

						function searchNode(nodeText) {

							var getSearchVal = nodeText;

							$(json.nodes).each(function(e) {
								if (json.nodes[e].name === getSearchVal) {
									$('.ipAdd').filter(function(index) {
										return $(this).text() === getSearchVal;
									}).parents('g').attr('data', 'selected')
								}
							})

							$(json.links)
									.each(
											function(e) {

												var getLinkSourceIndex = json.links[e].source["index"];
												var getLinkTargetIndex = json.links[e].target["index"];

												var getLinkSourceName = json.links[e].source["name"];
												getLinkSourceName = getLinkSourceName
														.toString();

												var getLinkTargetName = json.links[e].target["name"];
												getLinkTargetName = getLinkTargetName
														.toString();

												if (getSearchVal == getLinkSourceName
														|| getSearchVal == getLinkTargetName) {

													$('g.gLink').eq(e).attr(
															'data', 'selected');
													$('g.gLink')
															.parent('g')
															.attr('class',
																	'activeNodes')
															.attr('data',
																	'activeNodes');

													if (getLinkSourceName == getSearchVal) {

														$('.ipAdd')
																.filter(
																		function(
																				index) {

																			return $(
																					this)
																					.text() == getLinkTargetName;

																		})
																.parents(
																		'g.node')
																.attr('data',
																		'selected');

													} else if (getLinkTargetName == getSearchVal) {
														$('.ipAdd')
																.filter(
																		function(
																				index) {
																			return $(
																					this)
																					.text() == getLinkSourceName;
																		})
																.parents(
																		'g.node')
																.attr('data',
																		'selected');
													}

												} else {

													$('g.gLink').eq(e).attr(
															'data', 'default');

												}

											})

						}

						var linkedByIndex = {};
						json.links
								.forEach(function(d) {
									linkedByIndex[d.source.index + ","
											+ d.target.index] = 1;
								});

						function isConnected(a, b) {
							console.log(linkedByIndex[a.index + "," + b.index]
									+ '||'
									+ linkedByIndex[b.index + "," + a.index]
									+ '||' + a.index + '==' + b.index)
							return linkedByIndex[a.index + "," + b.index]
									|| linkedByIndex[b.index + "," + a.index]
									|| a.index == b.index;
						}
					}

					/* drawGraph(data); */

					/*
					 * $('.menuItems').each(function () {
					 * 
					 * var that = $(this);
					 * 
					 * $(that).mouseover(function () {
					 * $(this).addClass('activeMenu'); })
					 * 
					 * 
					 * 
					 * $(that).click(function () {
					 * $(that).addClass('activeMenu');
					 * 
					 * var dropdownThis = $(this); var styles = { top:
					 * $(dropdownThis).offset().top + 'px', left:
					 * $(dropdownThis).offset().left + 'px', height:
					 * $('.nodeExternalMenu > ul').height() + 'px', width:
					 * $('.nodeExternalMenu > ul').width() + 'px'
					 *  } $('.nodeExternalMenu').css(styles);
					 * $('.nodeExternalMenu').addClass('showMenu');
					 * 
					 * $('.nodeExternalMenu').mouseover(function () {
					 * $('.nodeExternalMenu').addClass('showMenu');
					 * $('.dropdown').removeClass('open');
					 * $(dropdownThis).addClass('open');
					 * $('.menuItems').removeClass('activeMenu');
					 * $(that).addClass('activeMenu');
					 *  })
					 * 
					 * $('.nodeExternalMenu').mouseout(function () {
					 * $('.nodeExternalMenu').removeClass('showMenu');
					 * $(dropdownThis).removeClass('open');
					 * $('.menuItems').removeClass('activeMenu'); })
					 * 
					 * $('.nodeExternalMenu li a').click(function () {
					 * $('.nodeExternalMenu').removeClass('showMenu');
					 * $(dropdownThis).removeClass('open');
					 * $('.menuItems').removeClass('activeMenu'); })
					 *  })
					 *  })
					 */

				});