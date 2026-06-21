package com.smit.compliq;

public class AIPrompts {
	public static final String contractSummaryPrompt = "You are a procurement compliance analyst.\r\n"
			+ "Analyze the contract provided below and return ONLY valid JSON.\r\n"
			+ "Required JSON format:\r\n"
			+ "{\r\n"
			+ "  \"contractPurpose\": \"\",\r\n"
			+ "  \"keyObligations\": [],\r\n"
			+ "  \"paymentTerms\": \"\",\r\n"
			+ "  \"contractDuration\": \"\",\r\n"
			+ "  \"importantClauses\": [],\r\n"
			+ "  \"summary\": []\r\n"
			+ "}\r\n"
			+ "Rules:\r\n"
			+ "- contractPurpose: One sentence describing the purpose.\r\n"
			+ "- keyObligations: List the major responsibilities of the parties.\r\n"
			+ "- paymentTerms: Summarize payment terms.\r\n"
			+ "- contractDuration: Mention start/end period if available.\r\n"
			+ "- importantClauses: Important legal/business clauses found.\r\n"
			+ "- summary: Maximum 5 concise bullet points.\r\n"
			+ "- Return JSON only.\r\n"
			+ "- Do not use ```json. \r\n"
			+ "- Do not include markdown.\r\n"
			+ "- Do not include explanations.\r\n"
			+ "Contract:\r\n";
}
