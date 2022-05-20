package java

const bytesConstTpl = `{{ $f := .Field }}{{ $r := .Rules -}}
{{- if $r.Const }}
		private final com.google.protobuf.ByteString {{ constantName . "Const" }} = com.google.protobuf.ByteString.copyFrom({{ byteArrayLit $r.GetConst }});
{{- end -}}
{{- if $r.In }}
		private final com.google.protobuf.ByteString[] {{ constantName . "In" }} = new com.google.protobuf.ByteString[]{
			{{- range $r.In }}
			com.google.protobuf.ByteString.copyFrom({{ byteArrayLit . }}),
			{{- end }}
		};
{{- end -}}
{{- if $r.NotIn }}
		private final com.google.protobuf.ByteString[] {{ constantName . "NotIn" }} = new com.google.protobuf.ByteString[]{
			{{- range $r.NotIn }}
			com.google.protobuf.ByteString.copyFrom({{ byteArrayLit . }}),
			{{- end }}
		};
{{- end -}}
{{- if $r.Pattern }}
		private final com.google.re2j.Pattern {{ constantName . "Pattern" }} = com.google.re2j.Pattern.compile({{ javaStringEscape $r.GetPattern }});
{{- end -}}
{{- if $r.Prefix }}
		private final byte[] {{ constantName . "Prefix" }} = {{ byteArrayLit $r.GetPrefix }};
{{- end -}}
{{- if $r.Contains }}
		private final byte[] {{ constantName . "Contains" }} = {{ byteArrayLit $r.GetContains }};
{{- end -}}
{{- if $r.Suffix }}
		private final byte[] {{ constantName . "Suffix" }} = {{ byteArrayLit $r.GetSuffix }};
{{- end -}}`

const bytesTpl = `{{ $f := .Field }}{{ $r := .Rules -}}
{{- if $r.GetIgnoreEmpty }}
			if ( !{{ accessor . }}.isEmpty() ) {
{{- end -}}
{{- if $r.Const }}
			io.circadence-official.pgv.ConstantValidation.constant("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ constantName . "Const" }});
{{- end -}}
{{- if $r.Len }}
			io.circadence-official.pgv.BytesValidation.length("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ $r.GetLen }});
{{- end -}}
{{- if $r.MinLen }}
			io.circadence-official.pgv.BytesValidation.minLength("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ $r.GetMinLen }});
{{- end -}}
{{- if $r.MaxLen }}
			io.circadence-official.pgv.BytesValidation.maxLength("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ $r.GetMaxLen }});
{{- end -}}
{{- if $r.Pattern }}
			io.circadence-official.pgv.BytesValidation.pattern("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ constantName . "Pattern" }});
{{- end -}}
{{- if $r.Prefix }}
			io.circadence-official.pgv.BytesValidation.prefix("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ constantName . "Prefix" }});
{{- end -}}
{{- if $r.Contains }}
			io.circadence-official.pgv.BytesValidation.contains("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ constantName . "Contains" }});
{{- end -}}
{{- if $r.Suffix }}
			io.circadence-official.pgv.BytesValidation.suffix("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ constantName . "Suffix" }});
{{- end -}}
{{- if $r.GetIp }}
			io.circadence-official.pgv.BytesValidation.ip("{{ $f.FullyQualifiedName }}", {{ accessor . }});
{{- end -}}
{{- if $r.GetIpv4 }}
			io.circadence-official.pgv.BytesValidation.ipv4("{{ $f.FullyQualifiedName }}", {{ accessor . }});
{{- end -}}
{{- if $r.GetIpv6 }}
			io.circadence-official.pgv.BytesValidation.ipv6("{{ $f.FullyQualifiedName }}", {{ accessor . }});
{{- end -}}
{{- if $r.In }}
			io.circadence-official.pgv.CollectiveValidation.in("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ constantName . "In" }});
{{- end -}}
{{- if $r.NotIn }}
			io.circadence-official.pgv.CollectiveValidation.notIn("{{ $f.FullyQualifiedName }}", {{ accessor . }}, {{ constantName . "NotIn" }});
{{- end -}}
{{- if $r.GetIgnoreEmpty }}
			}
{{- end -}}
`
