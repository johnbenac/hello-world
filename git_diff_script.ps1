$commits = git log --pretty=format:%h -- app/src/main/java/com/example/hello_world/ui/saved_conversations/view/SavedConversationsScreen.kt
$outputFile = "output.txt"
Remove-Item -Path $outputFile -ErrorAction SilentlyContinue
foreach ($commit in $commits) {
    $diff = git diff $commit..HEAD -- app/src/main/java/com/example/hello_world/ui/saved_conversations/view/SavedConversationsScreen.kt
    Add-Content -Path $outputFile -Value "Commit: $commit"
    Add-Content -Path $outputFile -Value $diff
}