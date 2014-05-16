class Jekyll::Page

  def relative
    "../" * (url.split("/").length-2)
  end

  def to_liquid(attrs = ATTRIBUTES_FOR_LIQUID)
    super(attrs + %w[
          relative
    ])

  end
end

class Jekyll::Post

  def relative
    "../" * (url.split("/").length-2)
  end

  def to_liquid(attrs = ATTRIBUTES_FOR_LIQUID)
    super(attrs + %w[
          relative
    ])

  end
end
