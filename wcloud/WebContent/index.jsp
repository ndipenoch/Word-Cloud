<%@ include file="includes/header.jsp"%>

<div class="animated bounceInDown"
	style="font-size: 48pt; font-family: arial; color: #990000; font-weight: bold">Web
	Opinion Visualiser</div>

</p>
&nbsp;
</p>
&nbsp;
</p>

<table width="600" cellspacing="0" cellpadding="7" border="0">
	<tr>
		<td valign="top">

			<form bgcolor="white" method="POST" action="doProcess">
				<fieldset>
					<legend>
						<h3>Specify Details</h3>
					</legend>

					<table>
						<tr>
							<th>Max. Edges</th>
							<th>Max. Display</th>
							<th>Goal Threshold</th>
							<th>AI Type</th>
						</tr>
						<tr>
							<td><select name="maxEdge">
									<option>20</option>
									<option selected>50</option>
									<option>100</option>
									<option>150</option>
									<option>200</option>
									<option>250</option>
									<option>300</option>
									<option>500</option>
									<option>750</option>
									<option>1000</option>
									<option>750</option>
									<option>5000</option>
							</select></td>

							<td><select name="maxDisplay">
									<option>20</option>
									<option selected>30</option>
									<option>60</option>
									<option>90</option>
									<option>100</option>
									<option>150</option>
									<option>200</option>
									<option>250</option>
							</select></td>
							
							<td><select name="goalThreshold">
									<option>3</option>
									<option selected>10</option>
									<option>5</option>
									<option>7</option>
									<option>15</option>
									<option>20</option>
									<option>25</option>
									<option>30</option>
									<option>35</option>
									<option>40</option>
									<option>45</option>
									<option>50</option>
							</select></td>

							<td><select name="aiType">
									<option>Neural Network</option>
									<option selected>Fuzzy Logic</option>
							</select></td>

						</tr>

					</table>
					<p></p>

					 Select the maximum edges to search, maximum words you want to display, and the AI type to use above.
					<p>
					<ol>
						<li><a href="https://jsoup.org">JSoup</a>
						<li><a
							href="http://jfuzzylogic.sourceforge.net/html/index.html">JFuzzyLogic</a>
						<li><a href="https://github.com/jeffheaton/encog-java-core">Encog</a>
					</ol>


					<p />

					<b>Enter Text :</b><br> <input name="query" size="100">
					<p />

					<center>
						<input type="submit" value="Search & Visualise!">
					</center>
				</fieldset>
			</form>

		</td>
	</tr>
</table>
<%@ include file="includes/footer.jsp"%>

